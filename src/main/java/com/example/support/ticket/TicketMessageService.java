
package com.example.support.ticket;

import com.example.support.user.User;
import com.example.support.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
public class TicketMessageService {

    private final TicketMessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketMessageService(
            TicketMessageRepository messageRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository) {

        this.messageRepository = messageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // Read permission:
    // Ticket creators can read their own conversations.
    // Support agents can read all ticket conversations.
    @Transactional(readOnly = true)
    public List<TicketMessage> getMessages(
            Long ticketId, String username) {

        Ticket ticket = getTicket(ticketId);
        User user = getUser(username);

        if (!isTicketCreator(ticket, user)
                && !isSupportAgent(user)) {
            throw new AccessDeniedException(
                    "You are not authorised to view this conversation.");
        }

        return messageRepository
                .findByTicketIdOrderByCreatedAtAscIdAsc(
                        ticket.getId());
    }

    // Reply permission:
    // Ticket creators can reply to their own tickets.
    // Support agents can reply only to tickets assigned to them.
    public TicketMessage sendMessage(
            Long ticketId,
            String username,
            String messageText) {

        Ticket ticket = getTicket(ticketId);
        User sender = getUser(username);

        boolean isCreator = isTicketCreator(ticket, sender);

        boolean isAssignedAgent =
                isSupportAgent(sender)
                        && ticket.getAssignedTo() != null
                        && ticket.getAssignedTo().equals(sender.getId());

        if (!isCreator && !isAssignedAgent) {
            throw new AccessDeniedException(
                    "You can reply only to your own tickets "
                            + "or tickets assigned to you.");
        }

        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty.");
        }

        String trimmedMessage = messageText.trim();

        if (trimmedMessage.length() > 10000) {
            throw new IllegalArgumentException(
                    "Message cannot exceed 10,000 characters.");
        }

        TicketMessage message = new TicketMessage();
        message.setTicketId(ticket.getId());
        message.setSenderId(sender.getId());
        message.setMessage(trimmedMessage);
        message.setCreatedAt(
                OffsetDateTime.now(ZoneOffset.UTC));

        return messageRepository.save(message);
    }

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket could not be found."));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account could not be found."));
    }

    private boolean isTicketCreator(Ticket ticket, User user) {
        return ticket.getCreatedBy() != null
                && ticket.getCreatedBy().equals(user.getId());
    }

    private boolean isSupportAgent(User user) {
        return userRepository.findByRole("SUPPORT")
                .stream()
                .anyMatch(agent ->
                        agent.getId().equals(user.getId()));
    }


    @Transactional(readOnly = true)
    public boolean canSendMessage(Long ticketId, String username) {
        Ticket ticket = getTicket(ticketId);
        User user = getUser(username);

        boolean isCreator = isTicketCreator(ticket, user);

        boolean isAssignedAgent =
                isSupportAgent(user)
                        && ticket.getAssignedTo() != null
                        && ticket.getAssignedTo().equals(user.getId());

        return isCreator || isAssignedAgent;
    }

}
