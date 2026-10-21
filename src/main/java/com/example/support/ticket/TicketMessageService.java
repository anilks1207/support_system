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

    // Read permission validation logic
    @Transactional(readOnly = true)
    public List<TicketMessage> getMessages(Long ticketId, String username) {
        Ticket ticket = getTicket(ticketId);
        User user = getUser(username);

        // 1. Support and Admin teams have full read access override
        if (isSupportOrAdmin(user)) {
            return messageRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        }

        // 2. Maintenance team can view the ticket conversation regardless of assignment status
        if (isMaintenanceWorker(user)) {
            return messageRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        }

        // 3. Regular users can only view conversations for tickets they created
        if (isTicketCreator(ticket, user)) {
            return messageRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        }

        throw new AccessDeniedException("You are not authorised to view this conversation.");
    }

    // Reply permission validation logic
    public TicketMessage sendMessage(Long ticketId, String username, String messageText) {
        Ticket ticket = getTicket(ticketId);
        User sender = getUser(username);

        boolean hasWritePermission = false;

        // Rule 1: Support and Admin team can reply to any conversation
        if (isSupportOrAdmin(sender)) {
            hasWritePermission = true;
        }
        // Rule 2: Maintenance workers can reply ONLY if the ticket is assigned to them
        else if (isMaintenanceWorker(sender)) {
            if (ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(sender.getId())) {
                hasWritePermission = true;
            }
        }
        // Rule 3: Regular clients can reply only to tickets they created
        else if (isTicketCreator(ticket, sender)) {
            hasWritePermission = true;
        }

        if (!hasWritePermission) {
            throw new AccessDeniedException("You do not have permission to add messages to this conversation.");
        }

        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        String trimmedMessage = messageText.trim();
        if (trimmedMessage.length() > 10000) {
            throw new IllegalArgumentException("Message cannot exceed 10,000 characters.");
        }

        TicketMessage message = new TicketMessage();
        message.setTicketId(ticket.getId());
        message.setSenderId(sender.getId());
        message.setMessage(trimmedMessage);
        message.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public boolean canSendMessage(Long ticketId, String username) {
        try {
            Ticket ticket = getTicket(ticketId);
            User user = getUser(username);

            if (isSupportOrAdmin(user)) {
                return true;
            }
            if (isMaintenanceWorker(user)) {
                return ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(user.getId());
            }
            return isTicketCreator(ticket, user);
        } catch (Exception e) {
            return false;
        }
    }

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket could not be found."));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User account could not be found."));
    }

    private boolean isTicketCreator(Ticket ticket, User user) {
        return ticket.getCreatedBy() != null && ticket.getCreatedBy().equals(user.getId());
    }

    // Helper: Validates Support and Admin elevated management permissions
    private boolean isSupportOrAdmin(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole().toUpperCase();
        return "SUPPORT".equals(role) || "ADMIN".equals(role);
    }

    // Helper: Validates Maintenance operational permissions
    private boolean isMaintenanceWorker(User user) {
        if (user == null || user.getRole() == null) return false;
        return "MAINTENANCE".equals(user.getRole().toUpperCase());
    }
}
