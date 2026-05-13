package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.model.ItemRow;
import com.tfg.rentalplatform.client.ui.ChatMessageFactory;
import com.tfg.rentalplatform.client.ui.ChatViewFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.util.FormatUtils.toLong;
import static com.tfg.rentalplatform.client.util.FormatUtils.toLongOrNull;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;

public final class ChatController {

    private final ApiClient api;
    private final ObservableList<Long> conversations;
    private final Map<Long, Map<String, Object>> rowsById = new HashMap<>();
    private final BooleanSupplier loggedCheck;
    private final BooleanSupplier adminRoleCheck;
    private final Consumer<Boolean> chatAlert;
    private final Runnable showChatView;
    private final Consumer<Long> openItemDetail;
    private final Consumer<Long> openPublicProfile;
    private final Runnable hideModal;
    private final Consumer<String> logger;

    private ListView<Long> conversationListView;
    private VBox messagesBox;
    private ScrollPane messagesScroll;
    private Label titleLabel;
    private Label subtitleLabel;
    private TextArea inputArea;
    private Long selectedConversationId;
    private boolean refreshingConversations;

    public ChatController(
            ApiClient api,
            ObservableList<Long> conversations,
            BooleanSupplier loggedCheck,
            BooleanSupplier adminRoleCheck,
            Consumer<Boolean> chatAlert,
            Runnable showChatView,
            Consumer<Long> openItemDetail,
            Consumer<Long> openPublicProfile,
            Runnable hideModal,
            Consumer<String> logger
    ) {
        this.api = api;
        this.conversations = conversations;
        this.loggedCheck = loggedCheck;
        this.adminRoleCheck = adminRoleCheck;
        this.chatAlert = chatAlert;
        this.showChatView = showChatView;
        this.openItemDetail = openItemDetail;
        this.openPublicProfile = openPublicProfile;
        this.hideModal = hideModal;
        this.logger = logger;
    }

    public Node createView() {
        ChatViewFactory.ChatViewParts view = ChatViewFactory.create(
                conversations,
                adminRoleCheck.getAsBoolean(),
                this::buildConversationCell,
                this::selectConversation,
                this::scrollToBottom,
                this::sendMessage
        );
        conversationListView = view.conversationListView();
        titleLabel = view.titleLabel();
        subtitleLabel = view.subtitleLabel();
        messagesBox = view.messagesBox();
        messagesScroll = view.messagesScroll();
        inputArea = view.inputArea();
        loadConversations();
        if (selectedConversationId != null) {
            loadMessages(selectedConversationId);
        }
        return view.root();
    }

    public void selectConversation(Long conversationId) {
        if (refreshingConversations) {
            return;
        }
        selectedConversationId = conversationId;
        if (selectedConversationId != null) {
            renderSelectedConversationHeader();
            loadMessages(selectedConversationId);
        }
    }

    public void openForReservation(Long reservationId) {
        if (reservationId == null) {
            logger.accept("Selecciona una reserva para abrir su chat.");
            return;
        }
        try {
            Map<String, Object> conversation = api.post("/api/chats/reservations/" + reservationId, Map.of());
            selectedConversationId = toLong(conversation.get("conversationId"));
            showChatView.run();
        } catch (Exception ex) {
            logger.accept("Error al abrir chat: " + ex.getMessage());
        }
    }

    public void openForCatalogItem(ItemRow item) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (item == null) {
            logger.accept("Selecciona un objeto para contactar.");
            return;
        }
        try {
            Map<String, Object> conversation = api.post("/api/chats/items/" + item.id(), Map.of());
            selectedConversationId = toLong(conversation.get("conversationId"));
            hideModal.run();
            showChatView.run();
            logger.accept("Chat abierto con " + conversation.get("otherUserName"));
        } catch (Exception ex) {
            logger.accept("Error al abrir chat: " + ex.getMessage());
        }
    }

    public void openAdminChatForUser(Long userId, String userName) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            Map<String, Object> conversation = adminRoleCheck.getAsBoolean()
                    ? api.post("/api/chats/admin/users/" + userId, Map.of())
                    : api.post("/api/chats/admin", Map.of());
            selectedConversationId = toLong(conversation.get("conversationId"));
            loadConversations();
            if (conversationListView != null) {
                conversationListView.getSelectionModel().select(selectedConversationId);
            }
            showChatView.run();
            logger.accept("Chat abierto con " + nullableString(userName));
        } catch (Exception ex) {
            logger.accept("Error al abrir chat de administracion: " + ex.getMessage());
        }
    }

    public void loadConversations() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            Long previousSelection = selectedConversationId;
            List<Map<String, Object>> response = api.getList("/api/chats");
            rowsById.clear();
            long unreadTotal = response.stream()
                    .mapToLong(c -> toLong(c.get("unreadCount")))
                    .sum();
            chatAlert.accept(unreadTotal > 0);
            refreshingConversations = true;
            conversations.setAll(response.stream().map(c -> {
                Long conversationId = toLong(c.get("conversationId"));
                rowsById.put(conversationId, new HashMap<>(c));
                return conversationId;
            }).toList());
            if (conversationListView != null && previousSelection != null && rowsById.containsKey(previousSelection)) {
                selectedConversationId = previousSelection;
                conversationListView.getSelectionModel().select(previousSelection);
            }
            refreshingConversations = false;
            logger.accept("Conversaciones cargadas: " + conversations.size());
            if (selectedConversationId != null && rowsById.containsKey(selectedConversationId)) {
                renderSelectedConversationHeader();
            } else {
                selectedConversationId = null;
                clearPanel(conversations.isEmpty() ? "Todavia no hay conversaciones" : "Selecciona una conversacion");
            }
        } catch (Exception ex) {
            refreshingConversations = false;
            logger.accept("Error en chat: " + ex.getMessage());
        }
    }

    public void handleRealtimeMessage(Map<String, Object> message) {
        Long conversationId = toLong(message.get("conversationId"));
        if (selectedConversationId != null && selectedConversationId.equals(conversationId) && messagesBox != null) {
            loadMessages(conversationId);
        } else {
            chatAlert.accept(true);
            loadConversations();
        }
    }

    public void syncCurrentUserName(Long currentUserId, String currentUserName) {
        rowsById.values().forEach(row -> {
            if (currentUserId.equals(toLongOrNull(row.get("otherUserId")))) {
                row.put("otherUserName", currentUserName);
            }
        });
        if (conversationListView != null) {
            conversationListView.refresh();
            renderSelectedConversationHeader();
        }
    }

    public void clear() {
        conversations.clear();
        rowsById.clear();
        selectedConversationId = null;
        clearPanel("Selecciona una conversacion");
    }

    private ListCell<Long> buildConversationCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Long conversationId, boolean empty) {
                super.updateItem(conversationId, empty);
                if (empty || conversationId == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(ChatMessageFactory.conversationCard(rowsById.get(conversationId), adminRoleCheck.getAsBoolean()));
            }
        };
    }

    private void loadMessages(Long conversationId) {
        if (!loggedCheck.getAsBoolean() || conversationId == null || messagesBox == null) {
            return;
        }
        try {
            List<Map<String, Object>> response = api.getList("/api/chats/" + conversationId + "/messages");
            messagesBox.getChildren().clear();
            if (response.isEmpty()) {
                Label empty = new Label("Empieza la conversacion sobre este objeto.");
                empty.getStyleClass().add("chat-empty");
                messagesBox.getChildren().add(empty);
            } else {
                response.forEach(message -> messagesBox.getChildren().add(ChatMessageFactory.messageBubble(message)));
            }
            scrollToBottom();
            loadConversations();
        } catch (Exception ex) {
            logger.accept("Error al cargar mensajes: " + ex.getMessage());
        }
    }

    private void scrollToBottom() {
        if (messagesScroll == null || messagesBox == null) {
            return;
        }
        Platform.runLater(() -> {
            messagesBox.applyCss();
            messagesBox.layout();
            messagesScroll.applyCss();
            messagesScroll.layout();
            messagesScroll.setVvalue(messagesScroll.getVmax());
            Platform.runLater(() -> messagesScroll.setVvalue(messagesScroll.getVmax()));
        });
    }

    private void sendMessage() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (selectedConversationId == null) {
            logger.accept("Selecciona una conversacion para enviar un mensaje.");
            return;
        }
        String content = inputArea == null ? "" : inputArea.getText().trim();
        if (content.isBlank()) {
            logger.accept("Escribe un mensaje antes de enviar.");
            return;
        }
        try {
            api.post("/api/chats/" + selectedConversationId + "/messages", Map.of("content", content));
            inputArea.clear();
            loadMessages(selectedConversationId);
            logger.accept("Mensaje enviado");
        } catch (Exception ex) {
            logger.accept("Error al enviar mensaje: " + ex.getMessage());
        }
    }

    private void renderSelectedConversationHeader() {
        if (titleLabel == null || subtitleLabel == null || selectedConversationId == null) {
            return;
        }
        Map<String, Object> conversation = rowsById.get(selectedConversationId);
        if (conversation == null) {
            return;
        }
        boolean adminConversation = toLongOrNull(conversation.get("itemId")) == null;
        String otherUserName = nullableString(conversation.get("otherUserName"));
        titleLabel.setText(adminConversation && adminRoleCheck.getAsBoolean()
                ? "Chat con " + otherUserName
                : String.valueOf(conversation.get("itemTitle")));
        subtitleLabel.setText(adminConversation ? "Conversacion con administracion" : otherUserName);
        if (!adminConversation && !titleLabel.getStyleClass().contains("chat-header-link")) {
            titleLabel.getStyleClass().add("chat-header-link");
        } else if (adminConversation) {
            titleLabel.getStyleClass().remove("chat-header-link");
        }
        if (!subtitleLabel.getStyleClass().contains("chat-header-link")) {
            subtitleLabel.getStyleClass().add("chat-header-link");
        }
        titleLabel.setOnMouseClicked(adminConversation ? null : e -> openItemDetail.accept(toLongOrNull(conversation.get("itemId"))));
        subtitleLabel.setOnMouseClicked(e -> openPublicProfile.accept(toLong(conversation.get("otherUserId"))));
    }

    private void clearPanel(String message) {
        if (titleLabel != null) {
            titleLabel.setText(message);
            titleLabel.setOnMouseClicked(null);
            titleLabel.getStyleClass().remove("chat-header-link");
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText("Selecciona un chat para ver la conversacion.");
            subtitleLabel.setOnMouseClicked(null);
            subtitleLabel.getStyleClass().remove("chat-header-link");
        }
        if (messagesBox != null) {
            messagesBox.getChildren().clear();
            Label empty = new Label(message);
            empty.getStyleClass().add("chat-empty");
            messagesBox.getChildren().add(empty);
        }
    }
}
