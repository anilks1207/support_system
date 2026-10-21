package com.example.support.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login", autoLayout = false)
@PageTitle("Login | Support System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
                .set("background", "#eaf2ff")
                .set("font-family", "Arial, sans-serif");

        // Login card
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

        H2 title = new H2("Welcome back");
        title.getStyle()
                .set("color", "#1e40af")
                .set("font-size", "26px")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("text-align", "center");

        Paragraph subtitle = new Paragraph(
                "Sign in to access your Support System account."
        );
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "14px")
                .set("text-align", "center")
                .set("margin", "0 0 12px 0");

        // Preserve Spring Security login action
        loginForm.setAction("login");
        

        // Registration link
        Anchor registerLink = new Anchor(
                "register",
                "Don't have an account? Create one"
        );
        registerLink.getStyle()
                .set("color", "#2563eb")
                .set("font-size", "14px")
                .set("text-align", "center")
                .set("text-decoration", "none")
                .set("margin-top", "8px");

        card.add(title, subtitle, loginForm, registerLink);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean loginError = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error");

        loginForm.setError(loginError);
    }
}