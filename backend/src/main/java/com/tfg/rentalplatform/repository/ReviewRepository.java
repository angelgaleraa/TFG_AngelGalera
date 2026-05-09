package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<Review> findByItemOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Review> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    @Query("""
            select r from Review r
            left join r.item i
            where r.targetUser.id = :userId or i.owner.id = :userId
            order by r.createdAt desc
            """)
    List<Review> findReceivedByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Review> findByReservationId(Long reservationId);

    Optional<Review> findByReservationIdAndAuthorId(Long reservationId, Long authorId);
}
