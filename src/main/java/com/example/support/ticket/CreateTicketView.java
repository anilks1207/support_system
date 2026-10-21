
package com.example.support.ticket;

import com.example.support.user.User;
import com.example.support.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("tickets/new")
@PageTitle("Create Ticket")
@PermitAll
public class CreateTicketView extends VerticalLayout {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public CreateTicketView(
            TicketService ticketService,
            UserRepository userRepository) {

        this.ticketService = ticketService;
        this.userRepository = userRepository;

        setMaxWidth("700px");
        setWidthFull();
        setPadding(true);

        add(com.vaadin.flow.component.html.H2
                .class.cast(new com.vaadin.flow.component.html.H2("Create Support Ticket")));

        TextField subjectField = new TextField("Subject");
        subjectField.setRequired(true);
        subjectField.setMaxLength(200);
        subjectField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setRequired(true);
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("180px");

        Button submitButton = new Button("Submit Ticket", event -> {
            String subject = subjectField.getValue() == null
                    ? "" : subjectField.getValue().trim();

            String description = descriptionField.getValue() == null
                    ? "" : descriptionField.getValue().trim();

            if (subject.isEmpty() || description.isEmpty()) {
                Notification.show(
                        "Please enter both subject and description.");
                return;
            }

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                Notification.show("Please log in before creating a ticket.");
                return;
            }

            User user = userRepository
                    .findByUsername(authentication.getName())
                    .orElse(null);

            if (user == null) {
                Notification.show(
                        "Your user account could not be found.");
                return;
            }

            Ticket ticket = ticketService.createTicket(
                    subject,
                    description,
                    user.getId());

            Notification.show(
                    "Ticket created successfully: "
                            + ticket.getTicketNumber());

            subjectField.clear();
            descriptionField.clear();
        });

        add(subjectField, descriptionField, submitButton);
    }
}