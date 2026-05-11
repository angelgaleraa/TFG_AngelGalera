package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ProfileDtos;
import com.tfg.rentalplatform.dto.ReviewDtos;
import com.tfg.rentalplatform.entity.Item;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.ReviewRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileResponse me(AuthenticatedUser currentUser) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        return profileFor(user);
    }

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileResponse publicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        return profileFor(user);
    }

    private ProfileDtos.ProfileResponse profileFor(User user) {
        List<Item> items = itemRepository.findByOwnerIdAndOwnerRemovedFalseOrderByCreatedAtDesc(user.getId());
        List<ReviewDtos.ReviewResponse> reviews = reviewRepository.findReceivedByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ReviewDtos.ReviewResponse::from)
                .toList();
        double average = reviews.stream()
                .mapToInt(ReviewDtos.ReviewResponse::rating)
                .average()
                .orElse(0);

        return ProfileDtos.ProfileResponse.from(
                user,
                items.size(),
                items.stream().filter(item -> Boolean.TRUE.equals(item.getActive())).count(),
                reservationRepository.findByRenterIdOrderByCreatedAtDesc(user.getId()).size(),
                reservationRepository.findByItemOwnerIdOrderByCreatedAtDesc(user.getId()).size(),
                reviews,
                average
        );
    }

    @Transactional
    public ProfileDtos.ProfileResponse update(AuthenticatedUser currentUser, ProfileDtos.UpdateProfileRequest request) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        user.setPhone(blankToNull(request.phone()));
        user.setBio(blankToNull(request.bio()));
        return me(currentUser);
    }

    @Transactional
    public void changePassword(AuthenticatedUser currentUser, ProfileDtos.ChangePasswordRequest request) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw ApiException.badRequest("INVALID_CURRENT_PASSWORD", "La contraseña actual no es correcta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
