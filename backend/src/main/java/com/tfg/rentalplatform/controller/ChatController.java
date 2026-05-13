package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ChatDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<ChatDtos.ConversationResponse> conversations() {
        return chatService.conversations(securityUtils.currentUser());
    }

    @PostMapping("/items/{itemId}")
    public ChatDtos.ConversationResponse startForItem(@PathVariable Long itemId) {
        return chatService.startForItem(securityUtils.currentUser(), itemId);
    }

    @PostMapping("/reservations/{reservationId}")
    public ChatDtos.ConversationResponse startForReservation(@PathVariable Long reservationId) {
        return chatService.startForReservation(securityUtils.currentUser(), reservationId);
    }

    @PostMapping("/admin")
    public ChatDtos.ConversationResponse startWithAdmin() {
        return chatService.startWithAdmin(securityUtils.currentUser());
    }

    @PostMapping("/admin/users/{userId}")
    public ChatDtos.ConversationResponse startAsAdmin(@PathVariable Long userId) {
        return chatService.startAsAdmin(securityUtils.currentUser(), userId);
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatDtos.MessageResponse> messages(@PathVariable Long conversationId) {
        return chatService.messages(securityUtils.currentUser(), conversationId);
    }

    @PostMapping("/{conversationId}/messages")
    public ChatDtos.MessageResponse send(@PathVariable Long conversationId,
                                         @Valid @RequestBody ChatDtos.SendMessageRequest request) {
        return chatService.send(securityUtils.currentUser(), conversationId, request);
    }
}
