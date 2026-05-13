package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class SupportChatViewFactory {

    private SupportChatViewFactory() {
    }

    public static SupportChatParts create(Runnable close, Runnable contactSupport) {
        Label title = new Label("Soporte");
        title.getStyleClass().add("support-chat-title");
        Label subtitle = new Label("Inicia sesion aqui solo para contactar con administracion.");
        subtitle.getStyleClass().add("support-chat-subtitle");
        subtitle.setWrapText(true);

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("icon-btn");
        closeButton.setOnAction(e -> close.run());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField emailField = new TextField();
        emailField.setPromptText("Correo electronico");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contrasena");
        VBox loginFieldsBox = new VBox(8, emailField, passwordField);

        VBox messagesBox = new VBox(8);
        messagesBox.getStyleClass().add("support-chat-messages");
        messagesBox.setMinHeight(250);
        ScrollPane messagesScroll = new ScrollPane(messagesBox);
        messagesScroll.setFitToWidth(true);
        messagesScroll.setFitToHeight(false);
        messagesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messagesScroll.getStyleClass().addAll("surface-scroll", "support-chat-scroll");
        messagesScroll.setPrefHeight(260);
        messagesScroll.setVisible(false);
        messagesScroll.setManaged(false);

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Escribe tu mensaje");
        messageArea.setPrefRowCount(3);
        messageArea.setWrapText(true);
        messageArea.setVisible(false);
        messageArea.setManaged(false);
        messageArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                contactSupport.run();
            }
        });

        Button contactButton = new Button("Abrir chat");
        contactButton.getStyleClass().add("primary-btn");
        contactButton.setMaxWidth(Double.MAX_VALUE);
        contactButton.setOnAction(e -> contactSupport.run());

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("auth-status");
        statusLabel.setWrapText(true);

        VBox panel = new VBox(8, header, subtitle, loginFieldsBox, messagesScroll, messageArea, contactButton, statusLabel);
        panel.getStyleClass().add("support-chat-panel");

        return new SupportChatParts(
                panel,
                emailField,
                passwordField,
                loginFieldsBox,
                messagesBox,
                messagesScroll,
                messageArea,
                contactButton,
                statusLabel
        );
    }

    public record SupportChatParts(
            VBox root,
            TextField emailField,
            PasswordField passwordField,
            VBox loginFieldsBox,
            VBox messagesBox,
            ScrollPane messagesScroll,
            TextArea messageArea,
            Button contactButton,
            Label statusLabel
    ) {
    }
}
