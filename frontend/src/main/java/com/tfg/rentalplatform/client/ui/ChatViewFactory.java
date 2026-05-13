package com.tfg.rentalplatform.client.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.tfg.rentalplatform.client.ui.UiFactory.sectionTitle;

public final class ChatViewFactory {

    private ChatViewFactory() {
    }

    public static ChatViewParts create(
            ObservableList<Long> conversations,
            boolean adminRole,
            Supplier<ListCell<Long>> conversationCell,
            Consumer<Long> selectConversation,
            Runnable scrollToBottom,
            Runnable sendMessage
    ) {
        ListView<Long> conversationListView = new ListView<>(conversations);
        conversationListView.getStyleClass().addAll("data-list", "conversation-list");
        conversationListView.setCellFactory(view -> conversationCell.get());
        conversationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> selectConversation.accept(newValue));
        VBox.setVgrow(conversationListView, Priority.ALWAYS);

        HBox inboxHeader = new HBox(10, sectionTitle(adminRole ? "Usuarios" : "Conversaciones"));
        inboxHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(inboxHeader.getChildren().getFirst(), Priority.ALWAYS);

        VBox inbox = new VBox(12, inboxHeader, conversationListView);
        inbox.getStyleClass().add("chat-inbox");
        inbox.setMinWidth(360);
        inbox.setPrefWidth(390);
        inbox.setMaxWidth(420);

        Label titleLabel = new Label("Selecciona una conversacion");
        titleLabel.getStyleClass().add("chat-title");
        Label subtitleLabel = new Label("Tus mensajes apareceran aqui.");
        subtitleLabel.getStyleClass().add("chat-subtitle");
        VBox chatHeaderText = new VBox(3, titleLabel, subtitleLabel);
        chatHeaderText.getStyleClass().add("chat-header-text");

        VBox messagesBox = new VBox(10);
        messagesBox.getStyleClass().add("chat-messages");
        ScrollPane messagesScroll = new ScrollPane(messagesBox);
        messagesScroll.setFitToWidth(true);
        messagesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScroll.getStyleClass().add("surface-scroll");
        messagesBox.heightProperty().addListener((obs, oldValue, newValue) -> scrollToBottom.run());
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Escribe un mensaje");
        inputArea.setPrefRowCount(2);
        inputArea.setWrapText(true);
        inputArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                sendMessage.run();
            }
        });
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        Button sendButton = new Button("Enviar");
        sendButton.getStyleClass().add("primary-btn");
        sendButton.setOnAction(e -> sendMessage.run());

        HBox composer = new HBox(10, inputArea, sendButton);
        composer.setAlignment(Pos.BOTTOM_LEFT);
        composer.getStyleClass().add("chat-composer");

        VBox chatPanel = new VBox(12, chatHeaderText, messagesScroll, composer);
        chatPanel.getStyleClass().add("chat-panel");
        HBox.setHgrow(chatPanel, Priority.ALWAYS);

        HBox root = new HBox(14, inbox, chatPanel);
        root.getStyleClass().add("chat-layout");
        VBox.setVgrow(root, Priority.ALWAYS);

        return new ChatViewParts(root, conversationListView, titleLabel, subtitleLabel, messagesBox, messagesScroll, inputArea);
    }

    public record ChatViewParts(
            Node root,
            ListView<Long> conversationListView,
            Label titleLabel,
            Label subtitleLabel,
            VBox messagesBox,
            ScrollPane messagesScroll,
            TextArea inputArea
    ) {
    }
}
