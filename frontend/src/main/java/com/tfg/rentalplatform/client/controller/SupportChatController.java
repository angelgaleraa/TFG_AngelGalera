package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.ui.SupportChatViewFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.tfg.rentalplatform.client.util.FormatUtils.toLong;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;

public final class SupportChatController {

    private final ApiClient supportApi = new ApiClient("http://localhost:8081");
    private final Supplier<String> emailPrefill;
    private final Consumer<String> logger;
    private final TextField emailField;
    private final PasswordField passwordField;
    private final TextArea messageArea;
    private final Button contactButton;
    private final VBox root;
    private final VBox loginFieldsBox;
    private final VBox messagesBox;
    private final ScrollPane messagesScroll;
    private final Label statusLabel;

    private Long conversationId;
    private boolean loggedIn;
    private Timeline refreshTimeline;

    public SupportChatController(Supplier<String> emailPrefill, Consumer<String> logger) {
        this.emailPrefill = emailPrefill;
        this.logger = logger;
        SupportChatViewFactory.SupportChatParts view = SupportChatViewFactory.create(this::toggle, this::contactSupport);
        emailField = view.emailField();
        passwordField = view.passwordField();
        loginFieldsBox = view.loginFieldsBox();
        messagesBox = view.messagesBox();
        messagesScroll = view.messagesScroll();
        messageArea = view.messageArea();
        contactButton = view.contactButton();
        statusLabel = view.statusLabel();
        root = view.root();
    }

    public VBox root() {
        return root;
    }

    public void toggle() {
        boolean show = !root.isVisible();
        root.setVisible(show);
        root.setManaged(show);
        if (show) {
            emailField.setText(emailPrefill.get());
            statusLabel.setText("");
            if (loggedIn) {
                loadMessages();
                startRefresh();
            }
        } else {
            stopRefresh();
        }
    }

    private void contactSupport() {
        String message = messageArea.getText();
        if (!loggedIn) {
            loginFromWidget();
            return;
        }
        if (conversationId == null || message == null || message.isBlank()) {
            showStatus("Escribe un mensaje.");
            return;
        }
        contactButton.setDisable(true);
        contactButton.setText("Enviando...");
        try {
            supportApi.post("/api/chats/" + conversationId + "/messages", Map.of("content", message));
            messageArea.clear();
            loadMessages();
            showStatus("");
        } catch (Exception ex) {
            showStatus("Error: " + ex.getMessage());
            logger.accept("Error al contactar con soporte: " + ex.getMessage());
        } finally {
            contactButton.setDisable(false);
            contactButton.setText("Enviar");
        }
    }

    private void loginFromWidget() {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showStatus("Introduce email y contrasena.");
            return;
        }
        contactButton.setDisable(true);
        contactButton.setText("Abriendo...");
        try {
            Map<String, Object> response = supportApi.post("/api/auth/support-login", Map.of(
                    "email", email,
                    "password", password
            ));
            supportApi.setToken(String.valueOf(response.get("token")));
            conversationId = toLong(response.get("conversationId"));
            loggedIn = true;
            loginFieldsBox.setVisible(false);
            loginFieldsBox.setManaged(false);
            if (!root.getStyleClass().contains("support-chat-panel-open")) {
                root.getStyleClass().add("support-chat-panel-open");
            }
            messagesScroll.setVisible(true);
            messagesScroll.setManaged(true);
            messageArea.setVisible(true);
            messageArea.setManaged(true);
            contactButton.setText("Enviar");
            messageArea.setPromptText("Escribe a soporte");
            loadMessages();
            startRefresh();
            showStatus("");
        } catch (Exception ex) {
            showStatus("Error: " + ex.getMessage());
            contactButton.setText("Iniciar chat");
        } finally {
            contactButton.setDisable(false);
        }
    }

    private void loadMessages() {
        if (!loggedIn || conversationId == null) {
            return;
        }
        try {
            List<Map<String, Object>> messages = supportApi.getList("/api/chats/" + conversationId + "/messages");
            messagesBox.getChildren().clear();
            if (messages.isEmpty()) {
                Label empty = new Label("Todavia no hay mensajes. Escribe a soporte cuando quieras.");
                empty.getStyleClass().add("support-chat-empty");
                empty.setWrapText(true);
                messagesBox.getChildren().add(empty);
            } else {
                for (Map<String, Object> message : messages) {
                    boolean mine = Boolean.TRUE.equals(message.get("mine"));
                    Label bubble = new Label(nullableString(message.get("content")));
                    bubble.setWrapText(true);
                    bubble.getStyleClass().addAll("chat-bubble", mine ? "chat-bubble-mine" : "chat-bubble-other");
                    HBox line = new HBox(bubble);
                    line.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    messagesBox.getChildren().add(line);
                }
            }
            Platform.runLater(() -> {
                messagesBox.applyCss();
                messagesBox.layout();
                messagesScroll.setVvalue(messagesScroll.getVmax());
            });
        } catch (Exception ex) {
            showStatus("Error al cargar mensajes: " + ex.getMessage());
        }
    }

    private void startRefresh() {
        if (!loggedIn || conversationId == null) {
            return;
        }
        stopRefresh();
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (root.isVisible()) {
                loadMessages();
            }
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }
}
