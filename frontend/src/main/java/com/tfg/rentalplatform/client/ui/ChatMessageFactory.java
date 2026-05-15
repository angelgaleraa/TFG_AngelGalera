package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

import static com.tfg.rentalplatform.client.util.FormatUtils.*;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;
import static com.tfg.rentalplatform.client.util.TextUtils.previewText;

public final class ChatMessageFactory {

    private ChatMessageFactory() {
    }

    public static Node conversationCard(Map<String, Object> conversation, boolean adminRole) {
        if (conversation == null) {
            Label fallback = new Label("Conversacion");
            fallback.getStyleClass().add("conversation-title");
            return fallback;
        }

        boolean adminConversation = toLongOrNull(conversation.get("itemId")) == null;
        Label title = new Label(adminConversation && adminRole
                ? "Chat con " + nullableString(conversation.get("otherUserName"))
                : nullableString(conversation.get("itemTitle")));
        title.getStyleClass().add("conversation-title");
        title.setMaxWidth(Double.MAX_VALUE);

        long unreadCount = toLong(conversation.get("unreadCount"));
        HBox titleRow = new HBox(8, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        if (unreadCount > 0) {
            Label unread = new Label(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            unread.getStyleClass().add("conversation-unread-badge");
            titleRow.getChildren().add(unread);
        }

        Label participant = new Label(nullableString(conversation.get("otherUserName")));
        participant.getStyleClass().add("conversation-participant");

        String lastMessage = nullableString(conversation.get("lastMessage"));
        Label preview = new Label(lastMessage.isBlank() ? "Sin mensajes todavia" : previewText(lastMessage, 64));
        preview.getStyleClass().add(lastMessage.isBlank() ? "conversation-preview-empty" : "conversation-preview");
        preview.setWrapText(true);

        VBox content = new VBox(5, titleRow, participant, preview);
        content.getStyleClass().add("conversation-card");
        return content;
    }

    public static Node messageBubble(Map<String, Object> message) {
        boolean mine = Boolean.parseBoolean(String.valueOf(message.get("mine")));
        Label author = new Label(mine ? "Tu" : String.valueOf(message.get("senderName")));
        author.getStyleClass().add("chat-bubble-author");

        Label body = new Label(String.valueOf(message.get("content")));
        body.setWrapText(true);
        body.getStyleClass().add("chat-bubble-body");

        Label time = new Label(formatDateTime(message.get("createdAt")));
        time.getStyleClass().add("chat-bubble-time");

        VBox bubble = new VBox(4, author, body, time);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(mine ? "chat-bubble-mine" : "chat-bubble-other");
        bubble.setMaxWidth(430);

        HBox row = new HBox(bubble);
        row.getStyleClass().add("chat-message-row");
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
