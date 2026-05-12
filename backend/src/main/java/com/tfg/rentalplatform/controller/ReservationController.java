package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ReservationDtos;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ReservationDtos.ReservationResponse create(@Valid @RequestBody ReservationDtos.CreateReservationRequest request) {
        return reservationService.create(securityUtils.currentUser(), request);
    }

    @GetMapping("/me")
    public List<ReservationDtos.ReservationResponse> myReservations() {
        AuthenticatedUser currentUser = securityUtils.currentUser();
        return reservationService.myReservations(currentUser);
    }

    @GetMapping("/owner")
    public List<ReservationDtos.ReservationResponse> ownerReservations() {
        AuthenticatedUser currentUser = securityUtils.currentUser();
        return reservationService.reservationsForOwner(currentUser);
    }

    @PutMapping("/{reservationId}/accept")
    public ReservationDtos.ReservationResponse accept(@PathVariable Long reservationId) {
        return reservationService.accept(reservationId, securityUtils.currentUser());
    }

    @PutMapping("/{reservationId}/reject")
    public ReservationDtos.ReservationResponse reject(@PathVariable Long reservationId) {
        return reservationService.reject(reservationId, securityUtils.currentUser());
    }

    @PutMapping("/{reservationId}/cancel")
    public ReservationDtos.ReservationResponse cancel(@PathVariable Long reservationId) {
        return reservationService.cancel(reservationId, securityUtils.currentUser());
    }

    @PutMapping("/{reservationId}/complete")
    public ReservationDtos.ReservationResponse complete(@PathVariable Long reservationId) {
        return reservationService.complete(reservationId, securityUtils.currentUser());
    }

    @DeleteMapping("/{reservationId}/list")
    public void hideFromList(@PathVariable Long reservationId) {
        reservationService.hideFromList(reservationId, securityUtils.currentUser());
    }
}
