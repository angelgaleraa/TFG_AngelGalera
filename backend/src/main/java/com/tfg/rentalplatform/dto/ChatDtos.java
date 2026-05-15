package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.ChatMessage;
import com.tfg.rentalplatform.entity.Conversation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ChatDtos {

    public record SendMessageRequest(
            @NotBlank @Size(max = 1000) String content
    ) {}

    public record ConversationResponse(
            Long conversationId,
            Long reservationId,
            Long itemId,
            String itemTitle,
            Long otherUserId,
            String otherUserName,
            String role,
            String reservationStatus,
            String lastMessage,
            LocalDateTime lastMessageAt,
            long unreadCount
    ) {
        public static ConversationResponse from(Conversation conversation, Long currentUserId, ChatMessage lastMessage, long unreadCount) {
            boolean currentIsRenter = conversation.getRenter().getId().equals(currentUserId);
            boolean adminConversation = Boolean.TRUE.equals(conversation.getAdminConversation());
            return new ConversationResponse(
                    conversation.getId(),
                    conversation.getReservation() == null ? null : conversation.getReservation().getId(),
                    conversation.getItem() == null ? null : conversation.getItem().getId(),
                    adminConversation ? "Administracion" : conversation.getItem().getTitle(),
                    currentIsRenter ? conversation.getOwner().getId() : conversation.getRenter().getId(),
                    currentIsRenter ? conversation.getOwner().getName() : conversation.getRenter().getName(),
                    adminConversation ? "Administracion" : currentIsRenter ? "Inquilino" : "Propietario",
                    conversation.getReservation() == null ? null : conversation.getReservation().getStatus().name(),
                    lastMessage == null ? "Sin mensajes todavia" : lastMessage.getContent(),
                    lastMessage == null ? conversation.getUpdatedAt() : lastMessage.getCreatedAt(),
                    unreadCount
            );
        }
    }

    public record MessageResponse(
            Long id,
            Long conversationId,
            Long reservationId,
            Long senderId,
            String senderName,
            Long recipientId,
            String recipientName,
            String content,
            boolean mine,
            boolean readByRecipient,
            LocalDateTime createdAt
    ) {
        public static MessageResponse from(ChatMessage message, Long currentUserId) {
            return new MessageResponse(
                    message.getId(),
                    message.getConversation().getId(),
                    message.getConversation().getReservation() == null ? null : message.getConversation().getReservation().getId(),
                    message.getSender().getId(),
                    message.getSender().getName(),
                    message.getRecipient().getId(),
                    message.getRecipient().getName(),
                    message.getContent(),
                    message.getSender().getId().equals(currentUserId),
                    Boolean.TRUE.equals(message.getReadByRecipient()),
                    message.getCreatedAt()
            );
        }
    }
}
