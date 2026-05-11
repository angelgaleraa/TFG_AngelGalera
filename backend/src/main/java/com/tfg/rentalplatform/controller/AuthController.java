package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.AuthDtos;
import com.tfg.rentalplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthDtos.AuthResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/support-contact")
    public AuthDtos.SupportContactResponse contactSupport(@Valid @RequestBody AuthDtos.SupportContactRequest request) {
        return authService.contactSupport(request);
    }

    @PostMapping("/support-login")
    public AuthDtos.SupportLoginResponse supportLogin(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.supportLogin(request);
    }
}
