package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Payment;
import com.tfg.rentalplatform.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentDtos {

    public record PaymentResponse(
            Long id,
            Long reservationId,
            String itemTitle,
            LocalDate startDate,
            LocalDate endDate,
            Long payerId,
            Long receiverId,
            BigDecimal amount,
            PaymentStatus status,
            String method,
            String referenceCode,
            LocalDateTime createdAt
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getReservation().getId(),
                    payment.getReservation().getItem().getTitle(),
                    payment.getReservation().getStartDate(),
                    payment.getReservation().getEndDate(),
                    payment.getPayer().getId(),
                    payment.getReceiver().getId(),
                    payment.getAmount(),
                    payment.getStatus(),
                    payment.getMethod(),
                    payment.getReferenceCode(),
                    payment.getCreatedAt()
            );
        }
    }
}
