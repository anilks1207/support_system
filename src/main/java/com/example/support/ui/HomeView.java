package com.example.support.ui;

import com.example.support.ticket.CreateTicketView;
import com.example.support.ticket.MyTicketsView;
import com.example.support.ticket.SupportDashboardView;
import com.example.support.inventory.StockInventoryView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Support System")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        getStyle()
                .set("background-color", "var(--support-page-bg)")
                .set("font-family", "inherit")
                .set("color", "var(--support-text)")
                .set("overflow-y", "auto");

        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setMaxWidth("1000px");
        content.setPadding(true);
        content.setSpacing(true);

        content.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "40px 24px")
                .set("margin", "0 auto");

        H1 heading = new H1("Welcome to SupportDesk");
        heading.getStyle()
                .set("color", "#172B4D")
                .set("margin", "0")
                .set("font-size", "32px");

        Paragraph description = new Paragraph(
                "Manage support requests, track progress, and view stock information.");
        description.getStyle()
                .set("color", "#64748B")
                .set("font-size", "16px")
                .set("line-height", "1.6")
                .set("margin-top", "0");

        Set<String> roles = getCurrentRoles();

        boolean isSupport = hasRole(roles, "SUPPORT");
        boolean isMaintenance = hasRole(roles, "MAINTENANCE");
        boolean isAdmin = hasRole(roles, "ADMIN");

        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");

        if (isSupport || isAdmin) {
            cards.add(
                    createNavigationCard(
                            "Support Dashboard",
                            "View and manage support tickets.",
                            SupportDashboardView.class,
                            "Open Dashboard"
                    ),
                    createNavigationCard(
                            "Stock Inventory",
                            "View and manage stock items and quantities.",
                            StockInventoryView.class,
                            "View Stock"
                    )
            );
        } else if (isMaintenance) {
            cards.add(
                    createNavigationCard(
                            "Support Dashboard",
                            "View support tickets and their progress.",
                            SupportDashboardView.class,
                            "View Dashboard"
                    ),
                    createNavigationCard(
                            "Stock Inventory",
                            "View available stock items and quantities.",
                            StockInventoryView.class,
                            "View Stock"
                    )
            );
        } else {
            cards.add(
                    createNavigationCard(
                            "Create a Ticket",
                            "Need help? Submit a new support request.",
                            CreateTicketView.class,
                            "Create Ticket"
                    ),
                    createNavigationCard(
                            "My Tickets",
                            "View your requests and check their status.",
                            MyTicketsView.class,
                            "View My Tickets"
                    )
            );
        }

        Paragraph footer = new Paragraph(
                "SupportDesk · Your support, organized.");
        footer.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "12px")
                .set("margin-top", "32px");

        content.add(heading, description, cards, footer);
        add(content);
    }

    private Set<String> getCurrentRoles() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_")
                        ? role.substring(5)
                        : role)
                .collect(Collectors.toSet());
    }

    private boolean hasRole(Set<String> roles, String role) {
        return roles.contains(role);
    }

    private Div createNavigationCard(
            String title,
            String description,
            Class<? extends Component> target,
            String linkText) {

        Div card = new Div();
        card.setWidthFull();

        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-sizing", "border-box")
                .set("min-width", "240px")
                .set("flex", "1 1 240px")
                .set("box-shadow", "0 2px 8px rgba(23, 43, 77, 0.04)");

        H2 cardTitle = new H2(title);
        cardTitle.getStyle()
                .set("color", "#174EA6")
                .set("font-size", "20px")
                .set("margin", "0 0 12px 0");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle()
                .set("color", "#64748B")
                .set("font-size", "14px")
                .set("line-height", "1.6");

        RouterLink link = new RouterLink(linkText, target);
        link.getStyle()
                .set("display", "inline-block")
                .set("background-color", "#174EA6")
                .set("color", "#FFFFFF")
                .set("text-decoration", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "10px 16px")
                .set("border-radius", "6px")
                .set("margin-top", "12px");

        card.add(cardTitle, cardDescription, link);
        return card;
    }
}