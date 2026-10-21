package com.example.support.ticket;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.support.user.User;
import com.example.support.user.UserRepository;
import com.example.support.ui.MainLayout;
import com.vaadin.flow.component.AttachEvent;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "support/dashboard", layout = MainLayout.class)
@PageTitle("Support Dashboard")
@RolesAllowed({"SUPPORT", "MAINTENANCE", "ADMIN"})
public class SupportDashboardView extends VerticalLayout {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketConversationReadService conversationReadService;

    // Keep layout structures global so lifecycle threads can target them safely
    private final HorizontalLayout summaryCardsContainer = new HorizontalLayout();
    private final Grid<Ticket> grid = new Grid<>(Ticket.class, false);

    public SupportDashboardView(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketConversationReadService conversationReadService) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.conversationReadService = conversationReadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("support-page-content");

        // Page text headers setup
        H2 heading = new H2("Support Dashboard");
        heading.getStyle().set("color", "#174EA6").set("margin", "0");

        Span subtitle = new Span("Manage support requests, update statuses and assign agents.");
        subtitle.getStyle().set("color", "#64748B").set("font-size", "14px");

        add(heading, subtitle);

        // Prep summary layout frame
        summaryCardsContainer.setWidthFull();
        summaryCardsContainer.setSpacing(true);
        summaryCardsContainer.setPadding(false);
        summaryCardsContainer.getStyle()
                .set("flex-wrap", "nowrap")
                .set("overflow-x", "auto")
                .set("box-sizing", "border-box");
        add(summaryCardsContainer);

        H3 ticketsHeading = new H3("All Support Tickets");
        ticketsHeading.getStyle().set("color", "#172B4D").set("margin-bottom", "0");
        add(ticketsHeading);

        // Prep data table skeleton
        grid.setWidthFull();
        grid.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box");

        add(grid);
        setFlexGrow(1, grid);
    }

    // SAFE LIFECYCLE ATTACHMENT FETCH LOOP
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Clean out previous states to prevent component duplicate insertion errors
        summaryCardsContainer.removeAll();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLoggedInUser = (authentication != null) ? authentication.getName() : "Unknown";

        boolean canAssignTickets = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(auth -> {
                    String role = auth.getAuthority();
                    return role.equals("ROLE_SUPPORT") || role.equals("SUPPORT")
                            || role.equals("ROLE_ADMIN") || role.equals("ADMIN");
                });

        // Safe isolated backend gathers
        List<Ticket> tickets = new ArrayList<>();
        try {
            tickets = ticketRepository.findAll();
            if (tickets == null) tickets = new ArrayList<>();
        } catch (Exception ignored) {}

        List<User> supportAgents = new ArrayList<>();
//        try {
//            supportAgents = userRepository.findByRole("SUPPORT");
//            if (supportAgents == null) supportAgents = new ArrayList<>();
//        } catch (Exception ignored) {}

        try {
            supportAgents = userRepository.findByRole("MAINTENANCE");
            if (supportAgents == null) supportAgents = new ArrayList<>();
        } catch (Exception ignored) {}

        Map<Long, String> usernames = new HashMap<>();
        try {
            usernames = userRepository.findAll().stream()
                    .filter(u -> u != null && u.getId() != null)
                    .collect(Collectors.toMap(
                            User::getId,
                            u -> u.getUsername() == null ? "Unknown" : u.getUsername(),
                            (exist, replace) -> exist
                    ));
        } catch (Exception ignored) {}

        // Metrics distribution
        long totalTickets = tickets.size();
        long open = tickets.stream().filter(t -> t != null && "OPEN".equals(t.getStatus())).count();
        long progress = tickets.stream().filter(t -> t != null && "IN_PROGRESS".equals(t.getStatus())).count();
        long resolved = tickets.stream().filter(t -> t != null && "RESOLVED".equals(t.getStatus())).count();

        summaryCardsContainer.add(
                createSummaryCard("Total Tickets", totalTickets, "support-card-total", "#FFFFFF"),
                createSummaryCard("Open", open, "support-card-open", "#991B1B"),
                createSummaryCard("In Progress", progress, "support-card-inprogress", "#1E40AF"),
                createSummaryCard("Resolved", resolved, "support-card-resolved", "#166534")
        );

        // Rebuild clean immutable columns mapping
        grid.removeAllColumns();

        grid.addColumn(t -> t.getTicketNumber() == null ? "" : t.getTicketNumber())
                .setHeader("Ticket Number").setAutoWidth(true);

        grid.addColumn(t -> t.getSubject() == null ? "No Subject" : t.getSubject())
                .setHeader("Subject").setAutoWidth(true).setFlexGrow(1);

        final Map<Long, String> finalUsernames = usernames;
        grid.addColumn(t -> t.getCreatedBy() == null ? "Unknown" : finalUsernames.getOrDefault(t.getCreatedBy(), "Unknown"))
                .setHeader("Created By").setAutoWidth(true);

        grid.addComponentColumn(t -> {
            String state = (t.getStatus() == null) ? "Unknown" : t.getStatus();
            Span badge = new Span(state.replace("_", " "));
            String color = switch (state) {
                case "OPEN" -> "#991B1B";
                case "IN_PROGRESS" -> "#1E40AF";
                case "RESOLVED" -> "#166534";
                default -> "#64748B";
            };
            badge.getStyle().set("background-color", "#FFFFFF").set("color", color)
                    .set("border", "1px solid " + color).set("border-radius", "12px")
                    .set("padding", "4px 10px").set("font-size", "12px")
                    .set("font-weight", "600").set("white-space", "nowrap");
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        grid.addColumn(t -> t.getPriority() == null ? "NORMAL" : t.getPriority())
                .setHeader("Priority").setAutoWidth(true);

        grid.addColumn(t -> t.getCreatedAt() == null ? "" : t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))
                .setHeader("Created At").setAutoWidth(true);

        grid.addComponentColumn(t -> {
            ComboBox<String> box = new ComboBox<>();
            box.setItems("OPEN", "IN_PROGRESS", "RESOLVED");
            box.setValue(t.getStatus() == null ? "OPEN" : t.getStatus());
            box.setWidth("155px");
            box.setClearButtonVisible(false);

            Button save = new Button("Save", ev -> {
                if (box.getValue() == null) return;
                t.setStatus(box.getValue());
                t.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                ticketRepository.save(t);
                Notification.show("Ticket status updated.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                attachEvent.getUI().getPage().reload();
            });
            save.getStyle().set("background-color", "#174EA6").set("color", "#FFFFFF");
            return new HorizontalLayout(box, save);
        }).setHeader("Update Status").setAutoWidth(true);

        final List<User> finalAgents = supportAgents;
        final boolean finalCanAssign = canAssignTickets;
        grid.addComponentColumn(t -> {
            if (!finalCanAssign) {
                String name = t.getAssignedTo() == null ? "Unassigned" : finalUsernames.getOrDefault(t.getAssignedTo(), "Unknown");
                return new Span(name);
            }
            Select<Long> sel = new Select<>();
            List<Long> ids = finalAgents.stream().map(User::getId).filter(Objects::nonNull).toList();
            sel.setItems(ids);
            sel.setItemLabelGenerator(id -> id == null ? "Unknown" : finalUsernames.getOrDefault(id, "Unknown"));
            sel.setWidth("150px");
            if (t.getAssignedTo() != null && ids.contains(t.getAssignedTo())) {
                sel.setValue(t.getAssignedTo());
            }
            Button btn = new Button("Assign", ev -> {
                if (sel.getValue() == null) return;
                t.setAssignedTo(sel.getValue());
                t.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                ticketRepository.save(t);
                Notification.show("Ticket assigned.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                attachEvent.getUI().getPage().reload();
            });
            return new HorizontalLayout(sel, btn);
        }).setHeader("Assigned To").setAutoWidth(true);
        grid.addComponentColumn(t -> {
            Button navBtn = new Button("Conversation", ev -> attachEvent.getUI().navigate("ticket-conversation/" + t.getId()));
            navBtn.getStyle().set("background-color", "#174EA6").set("color", "#FFFFFF").set("font-weight", "600");
            return navBtn;
        }).setHeader("Messages").setAutoWidth(true);
        grid.addComponentColumn(t -> {
            Long agentId = t.getAssignedTo();
            if (agentId == null) return new Span("Unassigned");
            long count = 0;
            try {
                if (conversationReadService != null) {
                    count = conversationReadService.getUnreadCount(t.getId(), currentLoggedInUser);
                }
            } catch (Exception e) {
                return new Span("No access");
            }
            Span label = new Span(count == 0 ? "No unread messages" : count + " unread");
            label.getStyle().set("display", "inline-block").set("padding", "5px 10px").set("border-radius", "12px").set("font-size", "12px");
            if (count > 0) {
                label.getStyle().set("background-color", "#DBEAFE").set("color", "#174EA6").set("border", "1px solid #93C5FD");
            } else {
                label.getStyle().set("background-color", "#F1F5F9").set("color", "#64748B").set("border", "1px solid #E2E8F0");
            }
            return label;
        }).setHeader("Unread Messages").setAutoWidth(true);
        grid.setItems(tickets);
        grid.setPageSize(15);
    }
    private Div createSummaryCard(String label, long count, String colorClass, String textColor) {
        Div card = new Div();
        card.addClassNames("support-summary-card", colorClass);
        card.setWidthFull();
        String bg = switch (colorClass) {
            case "support-card-total" -> "#174EA6";
            case "support-card-open" -> "#FEF2F2";
            case "support-card-inprogress" -> "#EFF6FF";
            case "support-card-resolved" -> "#F0FDF4";
            default -> "#FFFFFF";
        };
        String border = switch (colorClass) {
            case "support-card-total" -> "#174EA6";
            case "support-card-open" -> "#FECACA";
            case "support-card-inprogress" -> "#BFDBFE";
            case "support-card-resolved" -> "#BBF7D0";
            default -> "#E2E8F0";
        };
        card.getStyle()
                .set("background-color", bg)
                .set("border", "1px solid " + border)
                .set("border-radius", "12px")
                .set("border-left", "4px solid " + border)
                .set("padding", "16px")
                .set("box-sizing", "border-box")
                .set("min-width", "140px")
                .set("flex", "1 1 160px")
                .set("box-shadow", "0 2px 6px rgba(23, 78, 166, 0.08)");
        Span title = new Span(label);
        title.getStyle().set("display", "block").set("color", textColor).set("font-size", "13px").set("font-weight", "600");
        H3 number = new H3(String.valueOf(count));
        number.getStyle().set("color", textColor).set("margin", "8px 0 0 0").set("font-size", "26px");
        card.add(title, number);
        return card;
    }
}