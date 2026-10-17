package com.example.support.ticket;

import com.example.support.user.User;
import com.example.support.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketConversationReadService {

    private final TicketConversationReadRepository readRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMessageService ticketMessageService;

    public TicketConversationReadService(
            TicketConversationReadRepository readRepository,
            TicketMessageRepository messageRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketMessageService ticketMessageService) {

        this.readRepository = readRepository;
        this.messageRepository = messageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketMessageService = ticketMessageService;
    }

    /**
     * Marks the latest messages in a conversation as read.
     * TicketMessageService checks whether the user may access the ticket.
     */
    public void markConversationAsRead(Long ticketId, String username) {

        // Check that the ticket exists.
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket could not be found."));

        // Check that the user exists.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User account could not be found."));

        // Verify that this user is allowed to access the conversation.
        ticketMessageService.getMessages(ticketId, username);

        // Find the latest message ID, or use zero if there are no messages.
        Long latestMessageId = messageRepository
                .findTopByTicketIdOrderByIdDesc(ticketId)
                .map(TicketMessage::getId)
                .orElse(0L);

        // Find or create the user's read record for this ticket.
        TicketConversationRead read = readRepository
                .findByTicketIdAndUserId(ticket.getId(), user.getId())
                .orElseGet(TicketConversationRead::new);

        read.setTicketId(ticket.getId());
        read.setUserId(user.getId());

        // Never move the read position backwards.
        Long previousReadId = read.getLastReadMessageId() == null
                ? 0L
                : read.getLastReadMessageId();

        read.setLastReadMessageId(
                Math.max(previousReadId, latestMessageId));

        readRepository.save(read);
    }

    /**
     * Returns the number of unread messages from other users
     * for a particular ticket.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long ticketId, String username) {

        // Verify that the ticket exists.
        ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket could not be found."));

        // Verify that the user exists.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User account could not be found."));

        // Check access before returning information about the conversation.
        ticketMessageService.getMessages(ticketId, username);

        Long lastReadMessageId = readRepository
                .findByTicketIdAndUserId(ticketId, user.getId())
                .map(TicketConversationRead::getLastReadMessageId)
                .orElse(0L);

        if (lastReadMessageId == null) {
            lastReadMessageId = 0L;
        }

        return readRepository.countUnreadMessages(
                ticketId,
                user.getId(),
                lastReadMessageId);
    }

    /**
     * Returns the combined unread message count for the supplied tickets.
     * Pass only ticket IDs the current user is allowed to access.
     */
    @Transactional(readOnly = true)
    public long getTotalUnreadCount(List<Long> ticketIds, String username) {

        if (ticketIds == null || ticketIds.isEmpty()) {
            return 0L;
        }

        return ticketIds.stream()
                .distinct()
                .mapToLong(ticketId -> getUnreadCount(ticketId, username))
                .sum();
    }
}
