package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Review;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReviewDtos {

    public record CreateReviewRequest(
            @NotNull Long reservationId,
            @NotNull @Min(0) @Max(5) Integer rating,
            @NotBlank @Size(max = 500) String comment
    ) {}

    public record UpdateReviewRequest(
            @NotNull @Min(0) @Max(5) Integer rating,
            @NotBlank @Size(max = 500) String comment
    ) {}

    public record ReviewResponse(
            Long id,
            Long reservationId,
            Long itemId,
            String itemTitle,
            LocalDate startDate,
            LocalDate endDate,
            Long targetUserId,
            String targetUserName,
            Long authorId,
            String authorName,
            Integer rating,
            String comment,
            LocalDateTime createdAt
    ) {
        public static ReviewResponse from(Review review) {
            return new ReviewResponse(
                    review.getId(),
                    review.getReservation() == null ? null : review.getReservation().getId(),
                    review.getItem() == null ? null : review.getItem().getId(),
                    review.getItem() == null ? null : review.getItem().getTitle(),
                    review.getReservation() == null ? null : review.getReservation().getStartDate(),
                    review.getReservation() == null ? null : review.getReservation().getEndDate(),
                    review.getTargetUser() == null ? null : review.getTargetUser().getId(),
                    review.getTargetUser() == null ? null : review.getTargetUser().getName(),
                    review.getAuthor().getId(),
                    review.getAuthor().getName(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt()
            );
        }
    }
}
