package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ChatDtos;
import com.tfg.rentalplatform.entity.*;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ChatMessageRepository;
import com.tfg.rentalplatform.repository.ConversationRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final ChatRealtimeService chatRealtimeService;

    @Transactional(readOnly = true)
    public List<ChatDtos.ConversationResponse> conversations(AuthenticatedUser currentUser) {
        return conversationRepository.findByOwnerIdOrRenterIdOrderByUpdatedAtDesc(currentUser.id(), currentUser.id())
                .stream()
                .map(conversation -> {
                    ChatMessage last = chatMessageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                            .orElse(null);
                    long unread = chatMessageRepository.countByConversationIdAndRecipientIdAndReadByRecipientFalse(
                            conversation.getId(), currentUser.id());
                    return ChatDtos.ConversationResponse.from(conversation, currentUser.id(), last, unread);
                })
                .sorted(Comparator.comparing(ChatDtos.ConversationResponse::lastMessageAt).reversed())
                .toList();
    }

    @Transactional
    public ChatDtos.ConversationResponse startForItem(AuthenticatedUser currentUser, Long itemId) {
        Item item = itemService.getEntity(itemId);
        if (item.getOwner().getId().equals(currentUser.id())) {
            throw ApiException.badRequest("CHAT_OWN_ITEM", "No puedes abrir chat contigo mismo");
        }
        User renter = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        Conversation conversation = conversationRepository.findByItemIdAndRenterId(item.getId(), currentUser.id())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setItem(item);
                    created.setOwner(item.getOwner());
                    created.setRenter(renter);
                    created.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(created);
                });
        return toResponse(conversation, currentUser.id());
    }

    @Transactional
    public ChatDtos.ConversationResponse startForReservation(AuthenticatedUser currentUser, Long reservationId) {
        Reservation reservation = getAllowedReservation(currentUser, reservationId);
        Conversation conversation = conversationRepository.findByReservationId(reservationId)
                .orElseGet(() -> conversationRepository.findByItemIdAndRenterId(
                        reservation.getItem().getId(), reservation.getRenter().getId()
                ).orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setItem(reservation.getItem());
                    created.setOwner(reservation.getItem().getOwner());
                    created.setRenter(reservation.getRenter());
                    created.setReservation(reservation);
                    created.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(created);
                }));
        if (conversation.getReservation() == null) {
            conversation.setReservation(reservation);
            conversation.setUpdatedAt(LocalDateTime.now());
        }
        return toResponse(conversation, currentUser.id());
    }

    @Transactional
    public ChatDtos.ConversationResponse startWithAdmin(AuthenticatedUser currentUser) {
        if (currentUser.role() == UserRole.ADMIN) {
            throw ApiException.badRequest("CHAT_ADMIN_TARGET_REQUIRED", "Selecciona un usuario para iniciar el chat");
        }
        User admin = userRepository.findFirstByRoleAndActiveTrueOrderByIdAsc(UserRole.ADMIN)
                .orElseThrow(() -> ApiException.notFound("ADMIN_NOT_FOUND", "No hay administradores disponibles"));
        return toResponse(getOrCreateAdminConversation(admin, getUser(currentUser.id())), currentUser.id());
    }

    @Transactional
    public ChatDtos.ConversationResponse startAsAdmin(AuthenticatedUser currentUser, Long userId) {
        if (currentUser.role() != UserRole.ADMIN) {
            throw ApiException.forbidden("ADMIN_REQUIRED", "Solo administracion puede abrir este chat");
        }
        if (currentUser.id().equals(userId)) {
            throw ApiException.badRequest("CHAT_SELF", "No puedes abrir chat contigo mismo");
        }
        User admin = getUser(currentUser.id());
        User user = getUser(userId);
        if (user.getRole() == UserRole.ADMIN) {
            throw ApiException.badRequest("CHAT_ADMIN_TO_ADMIN", "Selecciona un usuario de la plataforma");
        }
        return toResponse(getOrCreateAdminConversation(admin, user), currentUser.id());
    }

    @Transactional
    public List<ChatDtos.MessageResponse> messages(AuthenticatedUser currentUser, Long conversationId) {
        Conversation conversation = getAllowedConversation(currentUser, conversationId);
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        messages.stream()
                .filter(message -> message.getRecipient().getId().equals(currentUser.id()))
                .forEach(message -> message.setReadByRecipient(true));
        return messages.stream().map(message -> ChatDtos.MessageResponse.from(message, currentUser.id())).toList();
    }

    @Transactional
    public ChatDtos.MessageResponse send(AuthenticatedUser currentUser, Long conversationId, ChatDtos.SendMessageRequest request) {
        Conversation conversation = getAllowedConversation(currentUser, conversationId);
        User sender = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        User recipient = conversation.getRenter().getId().equals(currentUser.id())
                ? conversation.getOwner()
                : conversation.getRenter();

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.content().trim());
        ChatMessage saved = chatMessageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());

        ChatDtos.MessageResponse senderResponse = ChatDtos.MessageResponse.from(saved, currentUser.id());
        chatRealtimeService.sendToUser(recipient.getId(), ChatDtos.MessageResponse.from(saved, recipient.getId()));
        return senderResponse;
    }

    private ChatDtos.ConversationResponse toResponse(Conversation conversation, Long currentUserId) {
        ChatMessage last = chatMessageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);
        long unread = chatMessageRepository.countByConversationIdAndRecipientIdAndReadByRecipientFalse(
                conversation.getId(), currentUserId);
        return ChatDtos.ConversationResponse.from(conversation, currentUserId, last, unread);
    }

    private Conversation getOrCreateAdminConversation(User admin, User user) {
        return conversationRepository.findByAdminConversationTrueAndOwnerIdAndRenterId(admin.getId(), user.getId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setOwner(admin);
                    created.setRenter(user);
                    created.setAdminConversation(true);
                    created.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(created);
                });
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
    }

    private Conversation getAllowedConversation(AuthenticatedUser currentUser, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ApiException.notFound("CONVERSATION_NOT_FOUND", "Conversacion no encontrada"));
        boolean isRenter = conversation.getRenter().getId().equals(currentUser.id());
        boolean isOwner = conversation.getOwner().getId().equals(currentUser.id());
        if (!isRenter && !isOwner) {
            throw ApiException.forbidden("CHAT_FORBIDDEN", "No puedes acceder a este chat");
        }
        return conversation;
    }

    private Reservation getAllowedReservation(AuthenticatedUser currentUser, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        boolean isRenter = reservation.getRenter().getId().equals(currentUser.id());
        boolean isOwner = reservation.getItem().getOwner().getId().equals(currentUser.id());
        if (!isRenter && !isOwner) {
            throw ApiException.forbidden("CHAT_FORBIDDEN", "No puedes acceder a este chat");
        }
        return reservation;
    }
}
