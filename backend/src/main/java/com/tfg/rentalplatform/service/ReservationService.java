package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ReservationDtos;
import com.tfg.rentalplatform.entity.*;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final ChatRealtimeService chatRealtimeService;

    @Value("${app.platform.fee-percent:10}")
    private BigDecimal platformFeePercent;

    @Transactional
    public ReservationDtos.ReservationResponse create(AuthenticatedUser currentUser, ReservationDtos.CreateReservationRequest request) {
        Item item = itemService.getEntity(request.itemId());
        User renter = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario arrendatario no encontrado"));

        if (!Boolean.TRUE.equals(item.getActive())) {
            throw ApiException.badRequest("ITEM_NOT_ACTIVE", "El objeto no esta disponible");
        }
        if (item.getOwner().getId().equals(renter.getId())) {
            throw ApiException.badRequest("RESERVATION_OWN_ITEM", "No puedes reservar tu propio objeto");
        }
        if (!request.endDate().isAfter(request.startDate())) {
            throw ApiException.badRequest("RESERVATION_INVALID_DATES", "La fecha fin debe ser posterior a la fecha inicio");
        }

        // Evita reservas cruzadas mientras otra solicitud sigue pendiente o aceptada.
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                item.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED),
                request.startDate(),
                request.endDate()
        );
        if (overlaps) {
            throw ApiException.badRequest("RESERVATION_OVERLAP", "Ya existe una reserva en esas fechas");
        }

        // El total incluye los dias alquilados mas la comision configurada de la plataforma.
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        BigDecimal base = item.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal fee = base.multiply(platformFeePercent).divide(BigDecimal.valueOf(100));
        BigDecimal total = base.add(fee);

        Reservation reservation = new Reservation();
        reservation.setItem(item);
        reservation.setRenter(renter);
        reservation.setStartDate(request.startDate());
        reservation.setEndDate(request.endDate());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setPlatformFeePercent(platformFeePercent);
        reservation.setTotalPrice(total);
        reservation.setUpdatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        notificationService.create(item.getOwner().getId(), NotificationType.RESERVATION_CREATED,
                "Tienes una nueva solicitud de reserva para '" + item.getTitle() + "'");
        notificationService.create(renter.getId(), NotificationType.RESERVATION_CREATED,
                "Reserva creada para '" + item.getTitle() + "'");

        ReservationDtos.ReservationResponse response = ReservationDtos.ReservationResponse.from(saved);
        notifyReservationUpdateAfterCommit(response, item.getOwner().getId(), renter.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ReservationDtos.ReservationResponse> myReservations(AuthenticatedUser currentUser) {
        return reservationRepository.findByRenterIdAndHiddenByRenterFalseOrderByCreatedAtDesc(currentUser.id())
                .stream().map(ReservationDtos.ReservationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDtos.ReservationResponse> reservationsForOwner(AuthenticatedUser currentUser) {
        return reservationRepository.findByItemOwnerIdAndHiddenByOwnerFalseOrderByCreatedAtDesc(currentUser.id())
                .stream().map(ReservationDtos.ReservationResponse::from).toList();
    }

    @Transactional
    public void hideFromList(Long reservationId, AuthenticatedUser currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        boolean isRenter = reservation.getRenter().getId().equals(currentUser.id());
        boolean isOwner = reservation.getItem().getOwner().getId().equals(currentUser.id());
        if (!isRenter && !isOwner) {
            throw ApiException.forbidden("RESERVATION_FORBIDDEN", "No puedes modificar esta reserva");
        }
        if (isRenter) {
            reservation.setHiddenByRenter(true);
        }
        if (isOwner) {
            reservation.setHiddenByOwner(true);
        }
        reservation.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public ReservationDtos.ReservationResponse accept(Long reservationId, AuthenticatedUser currentUser) {
        Reservation reservation = reservationRepository.findByIdAndItemOwnerId(reservationId, currentUser.id())
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        // El propietario transforma una solicitud pendiente en reserva aceptada y se genera el pago.
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ApiException.badRequest("RESERVATION_NOT_PENDING", "Solo se pueden aceptar reservas pendientes");
        }
        reservation.setStatus(ReservationStatus.ACCEPTED);
        reservation.setUpdatedAt(LocalDateTime.now());
        paymentService.createCapturedPaymentForReservation(reservationId);
        notificationService.create(reservation.getRenter().getId(), NotificationType.RESERVATION_ACCEPTED,
                "Reserva aceptada para '" + reservation.getItem().getTitle() + "'");
        ReservationDtos.ReservationResponse response = ReservationDtos.ReservationResponse.from(reservation);
        notifyReservationUpdateAfterCommit(response, reservation.getRenter().getId());
        return response;
    }

    @Transactional
    public ReservationDtos.ReservationResponse reject(Long reservationId, AuthenticatedUser currentUser) {
        Reservation reservation = reservationRepository.findByIdAndItemOwnerId(reservationId, currentUser.id())
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ApiException.badRequest("RESERVATION_NOT_PENDING", "Solo se pueden rechazar reservas pendientes");
        }
        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setUpdatedAt(LocalDateTime.now());
        notificationService.create(reservation.getRenter().getId(), NotificationType.RESERVATION_REJECTED,
                "Reserva rechazada para '" + reservation.getItem().getTitle() + "'");
        ReservationDtos.ReservationResponse response = ReservationDtos.ReservationResponse.from(reservation);
        notifyReservationUpdateAfterCommit(response, reservation.getRenter().getId());
        return response;
    }

    @Transactional
    public ReservationDtos.ReservationResponse cancel(Long reservationId, AuthenticatedUser currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        // La cancelacion puede hacerla el arrendatario o el propietario, pero con reglas distintas.
        boolean isRenter = reservation.getRenter().getId().equals(currentUser.id());
        boolean isOwner = reservation.getItem().getOwner().getId().equals(currentUser.id());
        if (!isRenter && !isOwner) {
            throw ApiException.forbidden("RESERVATION_FORBIDDEN", "No puedes cancelar esta reserva");
        }
        ReservationStatus previousStatus = reservation.getStatus();
        if (reservation.getStatus() == ReservationStatus.REJECTED
                || reservation.getStatus() == ReservationStatus.CANCELED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw ApiException.badRequest("RESERVATION_ALREADY_CLOSED", "La reserva ya esta cerrada");
        }
        if (isOwner && reservation.getStatus() != ReservationStatus.ACCEPTED) {
            throw ApiException.badRequest("RESERVATION_OWNER_CANCEL_NOT_ACCEPTED", "Solo puedes cancelar reservas aceptadas");
        }
        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setUpdatedAt(LocalDateTime.now());
        // Si ya habia pago capturado, cancelar la reserva lo marca como reembolsado.
        if (previousStatus == ReservationStatus.ACCEPTED) {
            paymentService.refundPaymentForReservation(reservation.getId());
        }
        String itemTitle = reservation.getItem().getTitle();
        if (isOwner) {
            notificationService.create(reservation.getRenter().getId(), NotificationType.RESERVATION_CANCELED,
                    "El propietario ha cancelado la reserva de '" + itemTitle + "'");
            notificationService.create(reservation.getItem().getOwner().getId(), NotificationType.RESERVATION_CANCELED,
                    "Has cancelado la reserva de '" + itemTitle + "'");
        } else {
            notificationService.create(reservation.getItem().getOwner().getId(), NotificationType.RESERVATION_CANCELED,
                    "Reserva cancelada para '" + itemTitle + "'");
            notificationService.create(reservation.getRenter().getId(), NotificationType.RESERVATION_CANCELED,
                    "Reserva cancelada para '" + itemTitle + "'");
        }
        ReservationDtos.ReservationResponse response = ReservationDtos.ReservationResponse.from(reservation);
        notifyReservationUpdateAfterCommit(response, reservation.getItem().getOwner().getId(), reservation.getRenter().getId());
        return response;
    }

    @Transactional
    public ReservationDtos.ReservationResponse complete(Long reservationId, AuthenticatedUser currentUser) {
        Reservation reservation = reservationRepository.findByIdAndItemOwnerId(reservationId, currentUser.id())
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        if (reservation.getStatus() != ReservationStatus.ACCEPTED) {
            throw ApiException.badRequest("RESERVATION_NOT_ACCEPTED", "Solo se pueden completar reservas aceptadas");
        }
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setUpdatedAt(LocalDateTime.now());
        notificationService.create(reservation.getRenter().getId(), NotificationType.RESERVATION_COMPLETED,
                "Reserva completada para '" + reservation.getItem().getTitle() + "'");
        ReservationDtos.ReservationResponse response = ReservationDtos.ReservationResponse.from(reservation);
        notifyReservationUpdateAfterCommit(response, reservation.getRenter().getId());
        return response;
    }

    private void notifyReservationUpdateAfterCommit(ReservationDtos.ReservationResponse response, Long... userIds) {
        // Se notifica despues del commit para no enviar eventos de cambios que podrian revertirse.
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            for (Long userId : userIds) {
                chatRealtimeService.sendReservationUpdateToUser(userId, response);
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Long userId : userIds) {
                    chatRealtimeService.sendReservationUpdateToUser(userId, response);
                }
            }
        });
    }
}
