package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.PaymentDtos;
import com.tfg.rentalplatform.entity.NotificationType;
import com.tfg.rentalplatform.entity.Payment;
import com.tfg.rentalplatform.entity.PaymentStatus;
import com.tfg.rentalplatform.entity.Reservation;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.PaymentRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    @Transactional
    public Payment createCapturedPaymentForReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        return paymentRepository.findByReservationId(reservationId).orElseGet(() -> {
            Payment payment = new Payment();
            payment.setReservation(reservation);
            payment.setPayer(reservation.getRenter());
            payment.setReceiver(reservation.getItem().getOwner());
            payment.setAmount(reservation.getTotalPrice());
            payment.setMethod("SIMULATED_CARD");
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setReferenceCode("PAY-" + UUID.randomUUID());
            Payment saved = paymentRepository.save(payment);
            notificationService.create(saved.getPayer().getId(), NotificationType.PAYMENT_CAPTURED,
                    "Gasto registrado para '" + reservation.getItem().getTitle() + "'");
            notificationService.create(saved.getReceiver().getId(), NotificationType.PAYMENT_CAPTURED,
                    "Ingreso recibido por '" + reservation.getItem().getTitle() + "'");
            return saved;
        });
    }

    @Transactional
    public Payment refundPaymentForReservation(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND", "No existe pago para esta reserva"));
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return payment;
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        notificationService.create(payment.getPayer().getId(), NotificationType.PAYMENT_REFUNDED,
                "Reembolso recibido por '" + payment.getReservation().getItem().getTitle() + "'");
        notificationService.create(payment.getReceiver().getId(), NotificationType.PAYMENT_REFUNDED,
                "Ingreso devuelto por '" + payment.getReservation().getItem().getTitle() + "'");
        return payment;
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> myPayments(AuthenticatedUser currentUser) {
        return paymentRepository.findByPayerIdOrderByCreatedAtDesc(currentUser.id())
                .stream()
                .map(PaymentDtos.PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> ownerPayments(AuthenticatedUser currentUser) {
        return paymentRepository.findByReceiverIdOrderByCreatedAtDesc(currentUser.id())
                .stream()
                .map(PaymentDtos.PaymentResponse::from)
                .toList();
    }
}
