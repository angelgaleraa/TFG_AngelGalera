package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ReviewDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ReviewDtos.ReviewResponse create(@Valid @RequestBody ReviewDtos.CreateReviewRequest request) {
        return reviewService.create(securityUtils.currentUser(), request);
    }

    @PutMapping("/{reviewId}")
    public ReviewDtos.ReviewResponse update(@PathVariable Long reviewId,
                                            @Valid @RequestBody ReviewDtos.UpdateReviewRequest request) {
        return reviewService.update(securityUtils.currentUser(), reviewId, request);
    }

    @GetMapping("/reservation/{reservationId}/me")
    public ReviewDtos.ReviewResponse mineByReservation(@PathVariable Long reservationId) {
        return reviewService.mineByReservation(securityUtils.currentUser(), reservationId);
    }

    @GetMapping("/item/{itemId}")
    public List<ReviewDtos.ReviewResponse> byItem(@PathVariable Long itemId) {
        return reviewService.byItem(itemId);
    }

    @GetMapping("/profile/{userId}")
    public List<ReviewDtos.ReviewResponse> byProfile(@PathVariable Long userId) {
        return reviewService.byProfile(userId);
    }
}
