package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 4, max = 100) String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(@NotBlank String token) {}

    public record SupportContactRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank @Size(max = 800) String message
    ) {}

    public record SupportContactResponse(String message) {}

    public record SupportLoginResponse(String token, Long conversationId, UserResponse user) {}

    public record UserResponse(Long id, String name, String email, UserRole role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        }
    }

    public record AuthResponse(String token, UserResponse user) {}
}
