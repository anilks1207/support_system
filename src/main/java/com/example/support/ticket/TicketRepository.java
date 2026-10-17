
package com.example.support.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<Ticket> findByStatusOrderByCreatedAtDesc(String status);
}