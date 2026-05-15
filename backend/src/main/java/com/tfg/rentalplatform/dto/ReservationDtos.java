package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Reservation;
import com.tfg.rentalplatform.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationDtos {

    public record CreateReservationRequest(
            @NotNull Long itemId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate
    ) {}

    public record ReservationResponse(
            Long id,
            Long itemId,
            String itemTitle,
            Long ownerId,
            String ownerName,
            Long renterId,
            String renterName,
            LocalDate startDate,
            LocalDate endDate,
            ReservationStatus status,
            BigDecimal platformFeePercent,
            BigDecimal totalPrice,
            LocalDateTime createdAt
    ) {
        public static ReservationResponse from(Reservation r) {
            return new ReservationResponse(
                    r.getId(),
                    r.getItem().getId(),
                    r.getItem().getTitle(),
                    r.getItem().getOwner().getId(),
                    r.getItem().getOwner().getName(),
                    r.getRenter().getId(),
                    r.getRenter().getName(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getStatus(),
                    r.getPlatformFeePercent(),
                    r.getTotalPrice(),
                    r.getCreatedAt()
            );
        }
    }
}
