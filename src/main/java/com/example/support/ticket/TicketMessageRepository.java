package com.example.support.ticket;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageRepository
        extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findByTicketIdOrderByCreatedAtAscIdAsc(
            Long ticketId);
    Optional<TicketMessage> findTopByTicketIdOrderByIdDesc(
            Long ticketId);
}