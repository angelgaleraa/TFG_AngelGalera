package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByOwnerIdOrRenterIdOrderByUpdatedAtDesc(Long ownerId, Long renterId);
    Optional<Conversation> findByItemIdAndRenterId(Long itemId, Long renterId);
    Optional<Conversation> findByReservationId(Long reservationId);
    Optional<Conversation> findByAdminConversationTrueAndOwnerIdAndRenterId(Long ownerId, Long renterId);
}
