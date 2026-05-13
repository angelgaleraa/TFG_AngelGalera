package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ReportController {

    private final ApiClient api;
    private final BooleanSupplier loggedCheck;
    private final Runnable hideModal;
    private final Runnable loadNotifications;
    private final Consumer<String> logger;

    public ReportController(ApiClient api,
                            BooleanSupplier loggedCheck,
                            Runnable hideModal,
                            Runnable loadNotifications,
                            Consumer<String> logger) {
        this.api = api;
        this.loggedCheck = loggedCheck;
        this.hideModal = hideModal;
        this.loadNotifications = loadNotifications;
        this.logger = logger;
    }

    public void submit(String type, Long itemId, Long userId, String reason, String details) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (reason == null || reason.isBlank()) {
            logger.accept("Selecciona un motivo para enviar el reporte.");
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("type", type);
            body.put("reason", reason);
            body.put("details", details == null ? "" : details.trim());
            if (itemId != null) {
                body.put("itemId", itemId);
            }
            if (userId != null) {
                body.put("userId", userId);
            }
            api.post("/api/reports", body);
            logger.accept("Reporte enviado");
            hideModal.run();
            loadNotifications.run();
        } catch (Exception ex) {
            logger.accept("Error al enviar reporte: " + ex.getMessage());
        }
    }
}
