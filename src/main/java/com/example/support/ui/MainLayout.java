package com.example.support.ui;

import com.example.support.ticket.CreateTicketView;
import com.example.support.ticket.MyTicketsView;
import com.example.support.ticket.SupportDashboardView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.example.support.inventory.StockInventoryView;

@PermitAll
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        createHeader();
        createSidebar();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);

        header.getStyle()
                .set("background-color", "#174EA6")
                .set("box-sizing", "border-box")
                .set("padding", "16px 28px");

        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle().set("color", "#174EA6");

        H2 logo = new H2("SupportDesk");
        logo.getStyle()
                .set("color", "#174EA6")
                .set("font-size", "22px")
                .set("margin", "0");

        Span tagline = new Span("Support made simple");
        tagline.getStyle()
                .set("color", "#64748B")
                .set("font-size", "13px");

        VerticalLayout branding = new VerticalLayout(logo, tagline);
        branding.setPadding(false);
        branding.setSpacing(false);

        branding.getStyle()
                .set("gap", "2px");

//        addToNavbar(toggle, branding);

        //HorizontalLayout header = new HorizontalLayout(toggle, branding);
        header.setWidthFull();
        header.setAlignItems(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
        );
        header.setPadding(true);
        header.setSpacing(true);

        header.getStyle()
                .set("background-color", "#174EA6")
                .set("border-bottom", "2px solid #E2E8F0")
                .set("box-sizing", "border-box");

        addToNavbar(header);

//        H2 logo = new H2("SupportDesk");
        logo.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-size", "24px");

//        Span tagline = new Span("Support made simple");
        tagline.getStyle()
                .set("color", "#DBEAFE")
                .set("font-size", "14px");

        header.add(logo, tagline);
//        getNavbar().getStyle()
//                .set("background-color", "#FFFFFF")
//                .set("border-bottom", "1px solid #E2E8F0")
//                .set("padding", "8px 16px");

    }


    private void createSidebar() {
        SideNav navigation = new SideNav();

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated();

        boolean isSupport = false;
        boolean isMaintenance = false;
        boolean isAdmin = false;

        if (isAuthenticated) {
            for (var authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();

                if (role.equals("ROLE_SUPPORT") || role.equals("SUPPORT")) {
                    isSupport = true;
                }

                if (role.equals("ROLE_MAINTENANCE")
                        || role.equals("MAINTENANCE")) {
                    isMaintenance = true;
                }

                if (role.equals("ROLE_ADMIN") || role.equals("ADMIN")) {
                    isAdmin = true;
                }
            }
        }

        // Home: everyone
        navigation.addItem(
                new SideNavItem(
                        "Home",
                        HomeView.class,
                        VaadinIcon.HOME.create()
                )
        );

        // Create Ticket: normal users, support and admin
        if (!isMaintenance || isSupport || isAdmin) {
            navigation.addItem(
                    new SideNavItem(
                            "Create Ticket",
                            CreateTicketView.class,
                            VaadinIcon.PLUS_CIRCLE.create()
                    )
            );
        }

        // My Tickets: normal users and support/admin
        if (!isMaintenance || isSupport || isAdmin) {
            navigation.addItem(
                    new SideNavItem(
                            "My Tickets",
                            MyTicketsView.class,
                            VaadinIcon.TICKET.create()
                    )
            );
        }

        // Stock Inventory: support, maintenance and admin
        if (isSupport || isMaintenance || isAdmin) {
            navigation.addItem(
                    new SideNavItem(
                            "Stock Inventory",
                            StockInventoryView.class,
                            VaadinIcon.CUBES.create()
                    )
            );
        }

        // Support Dashboard: support, maintenance and admin
        if (isSupport || isMaintenance || isAdmin) {
            navigation.addItem(
                    new SideNavItem(
                            "Support Dashboard",
                            SupportDashboardView.class,
                            VaadinIcon.DASHBOARD.create()
                    )
            );
        }

        navigation.getStyle()
                .set("padding", "12px");

        VerticalLayout drawerContent = new VerticalLayout(navigation);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        drawerContent.getStyle()
                .set("background-color", "#FFFFFF");

        addToDrawer(drawerContent);
    }

}