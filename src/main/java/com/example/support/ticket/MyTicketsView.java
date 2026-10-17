package com.example.support.ticket;

import com.example.support.user.User;
import com.example.support.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "tickets", layout = com.example.support.ui.MainLayout.class)
@PageTitle("My Tickets")
@PermitAll
public class MyTicketsView extends VerticalLayout {

    private final TicketConversationReadService conversationReadService;

    public MyTicketsView(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketConversationReadService conversationReadService) {

        this.conversationReadService = conversationReadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("support-page-content");

        getStyle()
                .set("background-color", "var(--support-page-bg)")
                .set("font-family", "inherit")
                .set("color", "var(--support-text)")
                .set("box-sizing", "border-box");

        // Page heading
        H2 heading = new H2("My Tickets");
        heading.getStyle()
                .set("color", "#174EA6")
                .set("margin", "0");

        Span subtitle = new Span(
                "Track your support requests and see their latest status.");

        subtitle.getStyle()
                .set("color", "#64748B")
                .set("font-size", "14px");

        add(heading, subtitle);

        // Verify logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            Notification.show("Please log in to view your tickets.");
            return;
        }

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {
            Notification.show("Your user account could not be found.");
            return;
        }

        // Retrieve only tickets created by the logged-in user
        List<Ticket> tickets =
                ticketRepository.findByCreatedByOrderByCreatedAtDesc(
                        user.getId());

        Map<Long, String> usernames = userRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getUsername
                ));

        // Calculate ticket totals
        long total = tickets.size();

        long open = tickets.stream()
                .filter(t -> "OPEN".equals(t.getStatus()))
                .count();

        long inProgress = tickets.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                .count();

        long resolved = tickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()))
                .count();

        // Summary cards
        Div summaryCards = new Div(
                createSummaryCard(
                        "Total Tickets", total,
                        "support-card-total", "#FFFFFF"),
                createSummaryCard(
                        "Open", open,
                        "support-card-open", "#991B1B"),
                createSummaryCard(
                        "In Progress", inProgress,
                        "support-card-inprogress", "#1E40AF"),
                createSummaryCard(
                        "Resolved", resolved,
                        "support-card-resolved", "#166534")
        );

        summaryCards.addClassName("support-summary-cards");
        summaryCards.setWidthFull();

        summaryCards.getStyle()
                .set("display", "grid")
                .set("grid-template-columns",
                        "repeat(4, minmax(0, 1fr))")
                .set("gap", "12px")
                .set("align-items", "stretch")
                .set("box-sizing", "border-box");

        add(summaryCards);

        // Ticket table heading
        H3 listHeading = new H3("Your Support Requests");

        listHeading.getStyle()
                .set("color", "#172B4D")
                .set("margin-bottom", "0");

        // Ticket table
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.setWidthFull();

        grid.addColumn(Ticket::getTicketNumber)
                .setHeader("Ticket Number")
                .setAutoWidth(true);

        // Conversation button
        grid.addComponentColumn(ticket -> {
            Button conversationButton = new Button(
                    "Conversation",
                    event -> getUI().ifPresent(ui ->
                            ui.navigate(
                                    "ticket-conversation/" + ticket.getId()
                            )
                    )
            );

            conversationButton.getStyle()
                    .set("background-color", "#174EA6")
                    .set("color", "#FFFFFF");

            return conversationButton;
        }).setHeader("Messages").setAutoWidth(true);

        // Unread message count
        grid.addComponentColumn(ticket -> {
            long unreadCount = conversationReadService.getUnreadCount(
                    ticket.getId(),
                    username
            );

            Span unreadLabel = new Span(
                    unreadCount == 0
                            ? "No unread messages"
                            : unreadCount + (unreadCount == 1
                            ? " unread"
                            : " unread")
            );

            unreadLabel.getStyle()
                    .set("display", "inline-block")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "600")
                    .set("white-space", "nowrap");

            if (unreadCount > 0) {
                unreadLabel.getStyle()
                        .set("background-color", "#DBEAFE")
                        .set("color", "#174EA6")
                        .set("border", "1px solid #93C5FD");
            } else {
                unreadLabel.getStyle()
                        .set("background-color", "#F1F5F9")
                        .set("color", "#64748B")
                        .set("border", "1px solid #E2E8F0");
            }

            return unreadLabel;
        }).setHeader("Unread Messages").setAutoWidth(true);

        grid.addColumn(Ticket::getSubject)
                .setHeader("Subject")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Ticket status
        grid.addComponentColumn(ticket -> {
            String ticketStatus = ticket.getStatus() == null
                    ? "Unknown"
                    : ticket.getStatus();

            Span status = new Span(
                    ticketStatus.replace("_", " "));

            String color = switch (ticketStatus) {
                case "OPEN" -> "#B45309";
                case "IN_PROGRESS" -> "#2563EB";
                case "RESOLVED" -> "#15803D";
                case "CLOSED" -> "#64748B";
                default -> "#64748B";
            };

            status.getStyle()
                    .set("background-color", "#FFFFFF")
                    .set("color", color)
                    .set("border", "1px solid " + color)
                    .set("border-radius", "12px")
                    .set("padding", "4px 10px")
                    .set("font-size", "12px")
                    .set("font-weight", "600")
                    .set("white-space", "nowrap");

            return status;
        }).setHeader("Status").setAutoWidth(true);

        // Assigned support agent
        grid.addColumn(ticket -> {
                    if (ticket.getAssignedTo() == null) {
                        return "Unassigned";
                    }

                    return usernames.getOrDefault(
                            ticket.getAssignedTo(),
                            "Unknown");
                })
                .setHeader("Assigned To")
                .setAutoWidth(true);

        grid.addColumn(Ticket::getPriority)
                .setHeader("Priority")
                .setAutoWidth(true);

        grid.addColumn(ticket ->
                        ticket.getCreatedAt() == null
                                ? ""
                                : ticket.getCreatedAt().format(
                                DateTimeFormatter.ofPattern(
                                        "dd MMM yyyy HH:mm")))
                .setHeader("Created At")
                .setAutoWidth(true);

        grid.setItems(tickets);
        grid.setPageSize(15);
        grid.setAllRowsVisible(false);

        grid.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box");

        add(listHeading, grid);
        setFlexGrow(1, grid);
    }

    // Create an individual ticket summary card
    private Div createSummaryCard(
            String label,
            long count,
            String colorClass,
            String textColor) {

        Div card = new Div();
        card.addClassNames("support-summary-card", colorClass);
        card.setWidthFull();

        card.getStyle()
                .set("border", "1px solid transparent")
                .set("border-radius", "12px")
                .set("padding", "18px")
                .set("box-sizing", "border-box")
                .set("min-width", "0")
                .set("overflow", "hidden");

        Span labelText = new Span(label);

        labelText.getStyle()
                .set("display", "block")
                .set("color", textColor)
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("overflow-wrap", "anywhere");

        H3 countText = new H3(String.valueOf(count));

        countText.getStyle()
                .set("color", textColor)
                .set("font-size", "26px")
                .set("margin", "8px 0 0 0");

        card.add(labelText, countText);

        return card;
    }
}
