package com.example.support.ticket;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "OPEN",
            "IN_PROGRESS",
            "RESOLVED",
            "CLOSED"
    );

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket createTicket(
            String subject,
            String description,
            Long createdBy) {

        Ticket ticket = new Ticket();

        ticket.setTicketNumber(
                "TKT-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase()
        );

        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setStatus("OPEN");
        ticket.setPriority("MEDIUM");
        ticket.setCreatedBy(createdBy);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketStatus(Long ticketId, String newStatus) {

        if (newStatus == null ||
                !ALLOWED_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid ticket status: " + newStatus
            );
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket not found: " + ticketId
                ));

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return ticketRepository.save(ticket);
    }

}
