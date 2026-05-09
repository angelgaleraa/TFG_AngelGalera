package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Reservation;
import com.tfg.rentalplatform.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRenterIdOrderByCreatedAtDesc(Long renterId);
    List<Reservation> findByItemOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Reservation> findByRenterIdAndHiddenByRenterFalseOrderByCreatedAtDesc(Long renterId);
    List<Reservation> findByItemOwnerIdAndHiddenByOwnerFalseOrderByCreatedAtDesc(Long ownerId);

    List<Reservation> findByItemIdAndStatusIn(Long itemId, List<ReservationStatus> statuses);

    boolean existsByItemId(Long itemId);
    boolean existsByItemIdAndStatusIn(Long itemId, List<ReservationStatus> statuses);

    @Query("""
            select count(r) > 0
            from Reservation r
            where r.item.id = :itemId
              and r.status in :statuses
              and r.startDate < :endDate
              and r.endDate > :startDate
            """)
    boolean existsOverlappingReservation(
            @Param("itemId") Long itemId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Reservation> findByIdAndItemOwnerId(Long id, Long ownerId);
}
