package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ReviewDtos;
import com.tfg.rentalplatform.entity.NotificationType;
import com.tfg.rentalplatform.entity.Reservation;
import com.tfg.rentalplatform.entity.ReservationStatus;
import com.tfg.rentalplatform.entity.Review;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.ReviewRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewDtos.ReviewResponse create(AuthenticatedUser currentUser, ReviewDtos.CreateReviewRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> ApiException.notFound("RESERVATION_NOT_FOUND", "Reserva no encontrada"));
        if (!reservation.getRenter().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("REVIEW_FORBIDDEN", "Solo el inquilino puede valorar esta reserva");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw ApiException.badRequest("REVIEW_INVALID_STATE", "Solo se puede valorar una reserva completada");
        }
        if (reviewRepository.findByReservationId(request.reservationId()).isPresent()) {
            throw ApiException.badRequest("REVIEW_ALREADY_EXISTS", "Ya existe una reseña para esta reserva");
        }

        Review review = new Review();
        review.setReservation(reservation);
        review.setItem(reservation.getItem());
        review.setTargetUser(reservation.getItem().getOwner());
        review.setAuthor(userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado")));
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        Review saved = reviewRepository.save(review);

        notificationService.create(reservation.getItem().getOwner().getId(), NotificationType.REVIEW_CREATED,
                "Has recibido una nueva reseña para '" + reservation.getItem().getTitle() + "'");
        return ReviewDtos.ReviewResponse.from(saved);
    }

    @Transactional
    public ReviewDtos.ReviewResponse update(AuthenticatedUser currentUser, Long reviewId, ReviewDtos.UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("REVIEW_NOT_FOUND", "Reseña no encontrada"));
        if (!review.getAuthor().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("REVIEW_FORBIDDEN", "Solo puedes modificar tus propias reseñas");
        }
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        return ReviewDtos.ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewDtos.ReviewResponse mineByReservation(AuthenticatedUser currentUser, Long reservationId) {
        return reviewRepository.findByReservationIdAndAuthorId(reservationId, currentUser.id())
                .map(ReviewDtos.ReviewResponse::from)
                .orElseThrow(() -> ApiException.notFound("REVIEW_NOT_FOUND", "Reseña no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ReviewDtos.ReviewResponse> byItem(Long itemId) {
        return reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId)
                .stream()
                .map(ReviewDtos.ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDtos.ReviewResponse> byProfile(Long userId) {
        return reviewRepository.findReceivedByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewDtos.ReviewResponse::from)
                .toList();
    }
}
