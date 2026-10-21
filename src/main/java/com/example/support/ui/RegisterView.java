package com.example.support.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.example.support.user.UserService;

@Route("register")
@PageTitle("Register | Support System")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        // Page layout
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
                .set("background", "#eaf2ff")
                .set("font-family", "Arial, sans-serif");

        // Registration card
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("440px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH);

        card.getStyle()
                .set("background", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "32px")
                .set("box-shadow", "0 8px 30px rgba(30, 64, 175, 0.12)")
                .set("border", "1px solid #dbeafe")
                .set("box-sizing", "border-box");

        // Heading
        H2 title = new H2("Create your account");
        title.getStyle()
                .set("color", "#1e40af")
                .set("font-size", "26px")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("text-align", "center");

        Paragraph subtitle = new Paragraph(
                "Join the Support System to get help and track your tickets."
        );
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "14px")
                .set("text-align", "center")
                .set("margin", "0 0 12px 0");

        // Form fields
        TextField displayName = new TextField("Display name");
        displayName.setPlaceholder("Enter your full name");
        displayName.setRequired(true);
        displayName.setMaxLength(100);
        displayName.setWidthFull();

        TextField username = new TextField("Username");
        username.setPlaceholder("Choose a username");
        username.setRequired(true);
        username.setMaxLength(50);
        username.setWidthFull();

        EmailField email = new EmailField("Email address");
        email.setPlaceholder("you@example.com");
        email.setRequired(true);
        email.setMaxLength(255);
        email.setWidthFull();

        PasswordField password = new PasswordField("Password");
        password.setPlaceholder("Create a strong password");
        password.setRequired(true);
        password.setMinLength(12);
        password.setHelperText("Use at least 12 characters.");
        password.setWidthFull();

        // Create account button
        Button registerButton = new Button("Create account");
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background", "#2563eb")
                .set("color", "#ffffff")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("height", "44px")
                .set("margin-top", "8px");

        registerButton.getElement().setAttribute("theme", "primary");

        // Link to the existing login page
        Anchor loginLink = new Anchor("login", "Already have an account? Sign in");
        loginLink.getStyle()
                .set("color", "#2563eb")
                .set("font-size", "14px")
                .set("text-align", "center")
                .set("text-decoration", "none");

        // Add components to the card
        card.add(
                title,
                subtitle,
                displayName,
                username,
                email,
                password,
                registerButton,
                loginLink
        );

        add(card);

        // Existing registration functionality
        registerButton.addClickListener(event -> {

            if (displayName.isEmpty()
                    || username.isEmpty()
                    || email.isEmpty()
                    || email.isInvalid()
                    || password.isEmpty()) {

                Notification notification = Notification.show(
                        "Please complete all fields correctly. "
                                + "Password must contain at least 12 characters.",
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                userService.registerUser(
                        username.getValue().trim(),
                        email.getValue().trim(),
                        displayName.getValue().trim(),
                        password.getValue()
                );

                Notification notification = Notification.show(
                        "Account created successfully. You can now sign in.",
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                registerButton.setEnabled(false);

            } catch (IllegalArgumentException ex) {
                Notification notification = Notification.show(
                        ex.getMessage(),
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}