package com.tfg.rentalplatform.security;

import com.tfg.rentalplatform.entity.UserRole;

public record AuthenticatedUser(Long id, String email, UserRole role) {
}
