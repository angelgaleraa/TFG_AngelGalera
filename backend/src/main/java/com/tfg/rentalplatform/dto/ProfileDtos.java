package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileDtos {

    public record UpdateProfileRequest(
            @Size(min = 2, max = 120) String name,
            @Size(max = 30) String phone,
            @Size(max = 500) String bio
    ) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 4, max = 120) String newPassword
    ) {}

    public record ProfileResponse(
            Long id,
            String name,
            String email,
            String phone,
            String bio,
            UserRole role,
            LocalDateTime createdAt,
            long publishedItems,
            long activeItems,
            long rentalsAsRenter,
            long rentalsAsOwner,
            long receivedReviews,
            double averageRating,
            List<ReviewDtos.ReviewResponse> reviews
    ) {
        public static ProfileResponse from(
                User user,
                long publishedItems,
                long activeItems,
                long rentalsAsRenter,
                long rentalsAsOwner,
                List<ReviewDtos.ReviewResponse> reviews,
                double averageRating
        ) {
            return new ProfileResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getBio(),
                    user.getRole(),
                    user.getCreatedAt(),
                    publishedItems,
                    activeItems,
                    rentalsAsRenter,
                    rentalsAsOwner,
                    reviews.size(),
                    averageRating,
                    reviews
            );
        }
    }
}
