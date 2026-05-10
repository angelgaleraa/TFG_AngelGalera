package com.tfg.rentalplatform.security;

import com.tfg.rentalplatform.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw ApiException.unauthorized("AUTH_REQUIRED", "Authentication required");
        }
        return new AuthenticatedUser(principal.getId(), principal.getUsername(), principal.getRole());
    }
}
