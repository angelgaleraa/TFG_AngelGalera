package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ReviewController {

    private final ApiClient api;
    private final BooleanSupplier loggedCheck;
    private final Runnable hideModal;
    private final Runnable reloadReservations;
    private final Runnable loadProfile;
    private final Consumer<String> logger;

    public ReviewController(ApiClient api,
                            BooleanSupplier loggedCheck,
                            Runnable hideModal,
                            Runnable reloadReservations,
                            Runnable loadProfile,
                            Consumer<String> logger) {
        this.api = api;
        this.loggedCheck = loggedCheck;
        this.hideModal = hideModal;
        this.reloadReservations = reloadReservations;
        this.loadProfile = loadProfile;
        this.logger = logger;
    }

    public Map<String, Object> findForReservation(Long reservationId) {
        if (!loggedCheck.getAsBoolean() || reservationId == null) {
            return null;
        }
        try {
            return api.getMap("/api/reviews/reservation/" + reservationId + "/me");
        } catch (Exception ignored) {
            return null;
        }
    }

    public void save(Long reservationId, Long reviewId, Integer rating, String comment) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (comment == null || comment.isBlank()) {
            logger.accept("Escribe un comentario para guardar la resena.");
            return;
        }
        try {
            if (reviewId == null) {
                api.post("/api/reviews", Map.of(
                        "reservationId", reservationId,
                        "rating", rating,
                        "comment", comment
                ));
                logger.accept("Resena publicada");
            } else {
                api.put("/api/reviews/" + reviewId, Map.of(
                        "rating", rating,
                        "comment", comment
                ));
                logger.accept("Resena actualizada");
            }
            hideModal.run();
            reloadReservations.run();
            loadProfile.run();
        } catch (Exception ex) {
            logger.accept("Error al guardar resena: " + ex.getMessage());
        }
    }
}
