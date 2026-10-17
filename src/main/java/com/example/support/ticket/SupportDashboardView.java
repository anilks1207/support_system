package com.example.support.ticket;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.support.user.User;
import com.example.support.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "support/dashboard", layout = com.example.support.ui.MainLayout.class)
@PageTitle("Support Dashboard")
@RolesAllowed({"SUPPORT", "MAINTENANCE", "ADMIN"})
public class SupportDashboardView extends VerticalLayout {

    public SupportDashboardView(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketConversationReadService conversationReadService) {

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean canAssignTickets = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    String role = authority.getAuthority();

                    return role.equals("ROLE_SUPPORT")
                            || role.equals("SUPPORT")
                            || role.equals("ROLE_ADMIN")
                            || role.equals("ADMIN");
                });
        addClassName("support-page-content");

        getStyle()
                .set("background-color", "var(--support-page-bg)")
                .set("font-family", "inherit")
                .set("color", "var(--support-text)")
                .set("box-sizing", "border-box");

        // Page heading
        H2 heading = new H2("Support Dashboard");
        heading.getStyle()
                .set("color", "#174EA6")
                .set("margin", "0");

        Span subtitle = new Span(
                "Manage support requests, update statuses and assign agents.");
        subtitle.getStyle()
                .set("color", "#64748B")
                .set("font-size", "14px");

        add(heading, subtitle);

        // Retrieve tickets and support agents
        List<Ticket> tickets = ticketRepository.findAll();
        List<User> supportAgents = userRepository.findByRole("SUPPORT");

        Map<Long, String> usernames = userRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getUsername
                ));

        // Calculate ticket totals
        long totalTickets = tickets.size();

        long openTickets = tickets.stream()
                .filter(t -> "OPEN".equals(t.getStatus()))
                .count();

        long inProgressTickets = tickets.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                .count();

        long resolvedTickets = tickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()))
                .count();

        // Summary cards
        HorizontalLayout summaryCards = new HorizontalLayout(
                createSummaryCard(
                        "Total Tickets",
                        totalTickets,
                        "support-card-total",
                        "#FFFFFF"
                ),
                createSummaryCard(
                        "Open",
                        openTickets,
                        "support-card-open",
                        "#991B1B"
                ),
                createSummaryCard(
                        "In Progress",
                        inProgressTickets,
                        "support-card-inprogress",
                        "#1E40AF"
                ),
                createSummaryCard(
                        "Resolved",
                        resolvedTickets,
                        "support-card-resolved",
                        "#166534"
                )
        );

        summaryCards.setWidthFull();
        summaryCards.setSpacing(true);
        summaryCards.setPadding(false);
        summaryCards.setAlignItems(Alignment.STRETCH);

        summaryCards.getStyle()
                .set("flex-wrap", "nowrap")
                .set("overflow-x", "auto")
                .set("box-sizing", "border-box");

        add(summaryCards);

        // Ticket table heading
        H3 ticketsHeading = new H3("All Support Tickets");
        ticketsHeading.getStyle()
                .set("color", "#172B4D")
                .set("margin-bottom", "0");

        // Ticket table
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.setWidthFull();

        grid.addColumn(Ticket::getTicketNumber)
                .setHeader("Ticket Number")
                .setAutoWidth(true);

        grid.addColumn(Ticket::getSubject)
                .setHeader("Subject")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(ticket ->
                        usernames.getOrDefault(
                                ticket.getCreatedBy(), "Unknown"))
                .setHeader("Created By")
                .setAutoWidth(true);

        // Ticket status badge
        grid.addComponentColumn(ticket -> {
            String ticketStatus = ticket.getStatus() == null
                    ? "Unknown"
                    : ticket.getStatus();

            Span status = new Span(
                    ticketStatus.replace("_", " ")
            );

            String statusColor = switch (ticketStatus) {
                case "OPEN" -> "#991B1B";
                case "IN_PROGRESS" -> "#1E40AF";
                case "RESOLVED" -> "#166534";
                default -> "#64748B";
            };

            status.getStyle()
                    .set("background-color", "#FFFFFF")
                    .set("color", statusColor)
                    .set("border", "1px solid " + statusColor)
                    .set("border-radius", "12px")
                    .set("padding", "4px 10px")
                    .set("font-size", "12px")
                    .set("font-weight", "600")
                    .set("white-space", "nowrap");

            return status;
        }).setHeader("Status").setAutoWidth(true);

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

        // Update ticket status
        grid.addComponentColumn(ticket -> {
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.setItems("OPEN", "IN_PROGRESS", "RESOLVED");
            statusBox.setValue(ticket.getStatus());
            statusBox.setWidth("155px");
            statusBox.setClearButtonVisible(false);

            Button saveButton = new Button("Save", event -> {
                if (statusBox.getValue() == null) {
                    Notification.show("Please select a status.");
                    return;
                }

                ticket.setStatus(statusBox.getValue());
                ticket.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                ticketRepository.save(ticket);

                Notification.show("Ticket status updated successfully.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                getUI().ifPresent(ui ->
                        ui.access(() -> ui.getPage().reload()));
            });

            saveButton.getStyle()
                    .set("background-color", "#174EA6")
                    .set("color", "#FFFFFF");

            return new HorizontalLayout(statusBox, saveButton);
        }).setHeader("Update Status").setAutoWidth(true);

        // Assign ticket to a support agent
        grid.addComponentColumn(ticket -> {
            if (!canAssignTickets) {
                // Maintenance can see the assigned user, but cannot change it.
                String assignedUsername = ticket.getAssignedTo() == null
                        ? "Unassigned"
                        : usernames.getOrDefault(ticket.getAssignedTo(), "Unknown");

                return new Span(assignedUsername);
            }

            // Support and Admin can assign or reassign tickets.
            Select<Long> agentSelect = new Select<>();
            agentSelect.setItems(
                    supportAgents.stream().map(User::getId).toList()
            );
            agentSelect.setItemLabelGenerator(
                    id -> usernames.getOrDefault(id, "Unknown")
            );
            agentSelect.setPlaceholder("Select agent");
            agentSelect.setWidth("150px");

            if (ticket.getAssignedTo() != null) {
                agentSelect.setValue(ticket.getAssignedTo());
            }

            Button assignButton = new Button("Assign", event -> {
                if (agentSelect.getValue() == null) {
                    Notification.show("Please select a support agent.");
                    return;
                }

                ticket.setAssignedTo(agentSelect.getValue());
                ticket.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                ticketRepository.save(ticket);

                Notification.show("Ticket assigned successfully.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                getUI().ifPresent(ui ->
                        ui.access(() -> ui.getPage().reload())
                );
            });

            return new HorizontalLayout(agentSelect, assignButton);
        }).setHeader("Assigned To").setAutoWidth(true);


        // Open ticket conversation
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
                    .set("color", "#FFFFFF")
                    .set("font-weight", "600");

            return conversationButton;
        }).setHeader("Messages").setAutoWidth(true);

        // Unread messages for the assigned support agent
        grid.addComponentColumn(ticket -> {
            Long assignedAgentId = ticket.getAssignedTo();

            if (assignedAgentId == null) {
                Span label = new Span("Unassigned");
                label.getStyle()
                        .set("color", "#64748B")
                        .set("font-size", "12px");
                return label;
            }

            String assignedAgentUsername = usernames.get(assignedAgentId);

            if (assignedAgentUsername == null) {
                Span label = new Span("Unknown agent");
                label.getStyle()
                        .set("color", "#64748B")
                        .set("font-size", "12px");
                return label;
            }

            long unreadCount = conversationReadService.getUnreadCount(
                    ticket.getId(),
                    assignedAgentUsername
            );

            Span unreadLabel = new Span(
                    unreadCount == 0
                            ? "No unread messages"
                            : unreadCount + " unread"
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

        grid.setItems(tickets);
        grid.setAllRowsVisible(false);
        grid.setPageSize(15);

        grid.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box");

        add(ticketsHeading, grid);
        setFlexGrow(1, grid);
    }

    // Create a coloured ticket summary card
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
                .set("border-left", "4px solid currentColor")
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("box-sizing", "border-box")
                .set("min-width", "0")
                .set("flex", "1 1 0");

        Span title = new Span(label);
        title.getStyle()
                .set("display", "block")
                .set("color", textColor)
                .set("font-size", "13px")
                .set("font-weight", "600");

        H3 number = new H3(String.valueOf(count));
        number.getStyle()
                .set("color", textColor)
                .set("margin", "8px 0 0 0")
                .set("font-size", "26px");

        card.add(title, number);

        return card;
    }
}
