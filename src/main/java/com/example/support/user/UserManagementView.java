
package com.example.support.user;

import com.example.support.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.List;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("User Management")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final UserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final TextField displayName = new TextField("Display Name");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Temporary Password");
    private final ComboBox<String> role = new ComboBox<>("Role");
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UserManagementView(
            UserService userService,
            UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("User Management"));

        username.setRequired(true);
        displayName.setRequired(true);
        email.setRequired(true);
        password.setRequired(true);
        password.setHelperText("At least 12 characters");
        role.setItems("USER", "SUPPORT", "MAINTENANCE", "ADMIN");
        role.setValue("USER");
        role.setRequired(true);

        Button createButton = new Button("Create User", event -> createUser());

        FormLayout formRow = new FormLayout();

        formRow.setWidthFull();

        formRow.add(
                username,
                displayName,
                email,
                password,
                role,
                createButton
        );

        formRow.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("1000px", 3)
        );

        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        grid.addColumn(User::getDisplayName).setHeader("Display Name").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(User::getRole).setHeader("Role").setAutoWidth(true);
        grid.addColumn(user -> user.isEnabled() ? "Active" : "Disabled")
                .setHeader("Status").setAutoWidth(true);

        grid.setSizeFull();

        add(formRow, grid);
        refreshUsers();
    }

    private void createUser() {
        if (username.isEmpty() || displayName.isEmpty()
                || email.isEmpty() || password.isEmpty()
                || role.isEmpty()) {
            Notification.show("Please complete all fields.");
            return;
        }

        try {
            userService.registerUserByAdmin(
                    username.getValue(),
                    email.getValue(),
                    displayName.getValue(),
                    password.getValue(),
                    role.getValue());

            Notification.show("User created successfully.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            username.clear();
            displayName.clear();
            email.clear();
            password.clear();
            role.setValue("USER");

            refreshUsers();

        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            Notification.show("Only Admin can create users.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshUsers() {
        List<User> users = userRepository.findAll();
        grid.setItems(users);
    }
}
