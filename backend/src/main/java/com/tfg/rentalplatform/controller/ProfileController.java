package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ProfileDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ProfileDtos.ProfileResponse me() {
        return profileService.me(securityUtils.currentUser());
    }

    @PutMapping("/me")
    public ProfileDtos.ProfileResponse update(@Valid @RequestBody ProfileDtos.UpdateProfileRequest request) {
        return profileService.update(securityUtils.currentUser(), request);
    }

    @PutMapping("/me/password")
    public void changePassword(@Valid @RequestBody ProfileDtos.ChangePasswordRequest request) {
        profileService.changePassword(securityUtils.currentUser(), request);
    }

    @GetMapping("/{userId}")
    public ProfileDtos.ProfileResponse publicProfile(@PathVariable Long userId) {
        return profileService.publicProfile(userId);
    }
}
