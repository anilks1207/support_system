
package com.example.support.ticket;

import com.example.support.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(
        value = "ticket-conversation",
        layout = com.example.support.ui.MainLayout.class
)
@PageTitle("Ticket Conversation")
@PermitAll
public class TicketConversationView
        extends VerticalLayout
        implements HasUrlParameter<Long> {


    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMessageService messageService;
    private final TicketConversationReadService conversationReadService;

    private final H2 heading = new H2("Ticket Conversation");
    private final Div conversationContent = new Div();

    private Long currentTicketId;
    private String currentUsername;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public TicketConversationView(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketMessageService messageService,
            TicketConversationReadService conversationReadService) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.conversationReadService = conversationReadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("support-page-content");

        getStyle()
                .set("background-color", "var(--support-page-bg)")
                .set("box-sizing", "border-box");

        heading.getStyle()
                .set("color", "#174EA6")
                .set("margin", "0");

        add(
                heading,
                new Span(
                        "Read and reply to messages about your support ticket."
                ),
                conversationContent
        );

        conversationContent.setWidthFull();
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long ticketId) {

        currentTicketId = ticketId;

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            showError("Please log in to view this conversation.");
            return;
        }

        currentUsername = authentication.getName();
        loadConversation();
    }

    private void loadConversation() {

        conversationContent.removeAll();

        try {
            // Check access before displaying any ticket details.
            List<TicketMessage> messages =
                    messageService.getMessages(
                            currentTicketId,
                            currentUsername
                    );

            conversationReadService.markConversationAsRead(
                    currentTicketId,
                    currentUsername
            );

            Ticket ticket = ticketRepository.findById(currentTicketId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Ticket could not be found."
                            ));

            H3 ticketHeading = new H3(
                    ticket.getTicketNumber() + " - "
                            + ticket.getSubject()
            );

            ticketHeading.getStyle()
                    .set("color", "#172B4D")
                    .set("margin", "8px 0");

            Div messageList = new Div();
            messageList.setWidthFull();

            messageList.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "12px")
                    .set("padding", "16px")
                    .set("background-color", "#FFFFFF")
                    .set("border", "1px solid #E2E8F0")
                    .set("border-radius", "12px")
                    .set("box-sizing", "border-box");

            if (messages.isEmpty()) {
                Span emptyMessage = new Span(
                        "No messages yet. Send the first reply below."
                );

                emptyMessage.getStyle()
                        .set("color", "#64748B")
                        .set("padding", "12px 0");

                messageList.add(emptyMessage);
            } else {
                for (TicketMessage message : messages) {
                    messageList.add(createMessageCard(message));
                }
            }


            boolean canReply = messageService.canSendMessage(
                    currentTicketId,
                    currentUsername
            );

            if (canReply) {
                TextArea messageInput = new TextArea("Your message");
                messageInput.setPlaceholder("Write your reply here...");
                messageInput.setWidthFull();
                messageInput.setMaxLength(10000);
                messageInput.setMinHeight("110px");

                Button sendButton = new Button(
                        "Send Message",
                        event -> {
                            String text = messageInput.getValue();

                            if (text == null || text.isBlank()) {
                                Notification.show(
                                        "Please enter a message before sending."
                                );
                                return;
                            }

                            try {
                                messageService.sendMessage(
                                        currentTicketId,
                                        currentUsername,
                                        text
                                );

                                Notification.show("Message sent successfully.");
                                loadConversation();

                            } catch (AccessDeniedException ex) {
                                Notification.show(
                                        "You are not authorised to reply to this ticket."
                                );
                            } catch (IllegalArgumentException ex) {
                                Notification.show(ex.getMessage());
                            }
                        }
                );

                sendButton.getStyle()
                        .set("background-color", "#174EA6")
                        .set("color", "#FFFFFF")
                        .set("font-weight", "600");

                RouterLink backLink = new RouterLink(
                        "Back to My Tickets",
                        MyTicketsView.class
                );

                backLink.getStyle()
                        .set("color", "#2563EB")
                        .set("font-weight", "600");

                HorizontalLayout actions =
                        new HorizontalLayout(sendButton, backLink);

                actions.setAlignItems(Alignment.CENTER);
                actions.getStyle().set("flex-wrap", "wrap");

                conversationContent.add(
                        ticketHeading,
                        messageList,
                        messageInput,
                        actions
                );

            } else {
                Span notice = new Span(
                        "You can read this conversation, but only the ticket creator "
                                + "or the assigned support agent can reply."
                );

                notice.getStyle()
                        .set("color", "#64748B")
                        .set("background-color", "#EAF2FF")
                        .set("padding", "12px")
                        .set("border-radius", "8px");

                RouterLink backLink = new RouterLink(
                        "Back to My Tickets",
                        MyTicketsView.class
                );

                conversationContent.add(
                        ticketHeading,
                        messageList,
                        notice,
                        backLink
                );
            }


        } catch (AccessDeniedException ex) {
            showError(
                    "You are not authorised to view this ticket conversation."
            );
        } catch (IllegalArgumentException ex) {
            showError("The requested ticket could not be found.");
        }
    }

    private Div createMessageCard(TicketMessage message) {

        String senderName = userRepository.findById(message.getSenderId())
                .map(user -> user.getUsername())
                .orElse("Unknown user");

        Div card = new Div();
        card.setWidthFull();

        card.getStyle()
                .set("padding", "12px 14px")
                .set("border-radius", "10px")
                .set("background-color", "#F1F6FF")
                .set("border-left", "3px solid #2563EB")
                .set("box-sizing", "border-box");

        Span sender = new Span(senderName);

        sender.getStyle()
                .set("display", "block")
                .set("color", "#174EA6")
                .set("font-weight", "700")
                .set("font-size", "13px");

        Span timestamp = new Span(
                message.getCreatedAt() == null
                        ? ""
                        : message.getCreatedAt().format(dateFormatter)
        );

        timestamp.getStyle()
                .set("display", "block")
                .set("color", "#64748B")
                .set("font-size", "12px")
                .set("margin-top", "3px");

        Div body = new Div();
        body.setText(message.getMessage());

        body.getStyle()
                .set("color", "#172B4D")
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere")
                .set("margin-top", "10px");

        card.add(sender, timestamp, body);

        return card;
    }

    private void showError(String message) {
        conversationContent.removeAll();

        Span error = new Span(message);

        error.getStyle()
                .set("color", "#991B1B")
                .set("background-color", "#FEE2E2")
                .set("padding", "12px")
                .set("border-radius", "8px");

        RouterLink backLink = new RouterLink(
                "Back to My Tickets",
                MyTicketsView.class
        );

        conversationContent.add(error, backLink);
    }
}