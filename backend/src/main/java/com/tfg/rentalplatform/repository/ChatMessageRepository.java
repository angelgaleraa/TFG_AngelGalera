package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    long countByConversationIdAndRecipientIdAndReadByRecipientFalse(Long conversationId, Long recipientId);
}
