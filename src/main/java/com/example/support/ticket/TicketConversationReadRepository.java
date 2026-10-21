package com.example.support.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketConversationReadRepository
        extends JpaRepository<TicketConversationRead, Long> {

    Optional<TicketConversationRead> findByTicketIdAndUserId(
            Long ticketId,
            Long userId
    );

    @Query("""
        SELECT COUNT(m)
        FROM TicketMessage m
        WHERE m.ticketId = :ticketId
          AND m.id > :lastReadMessageId
          AND m.senderId <> :userId
        """)
    long countUnreadMessages(
            @Param("ticketId") Long ticketId,
            @Param("userId") Long userId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );
}
