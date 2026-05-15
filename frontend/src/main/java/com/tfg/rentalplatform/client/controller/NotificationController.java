package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class NotificationController {

    private final ApiClient api;
    private final ObservableList<Map<String, Object>> notificationRows;
    private final Consumer<Boolean> unreadStateHandler;
    private final Consumer<String> logger;

    public NotificationController(
            ApiClient api,
            ObservableList<Map<String, Object>> notificationRows,
            Consumer<Boolean> unreadStateHandler,
            Consumer<String> logger
    ) {
        this.api = api;
        this.notificationRows = notificationRows;
        this.unreadStateHandler = unreadStateHandler;
        this.logger = logger;
    }

    public void load() {
        try {
            List<Map<String, Object>> response = api.getList("/api/notifications/me");
            notificationRows.setAll(response);
            boolean hasUnread = response.stream().anyMatch(n -> !Boolean.TRUE.equals(n.get("read")));
            unreadStateHandler.accept(hasUnread);
            logger.accept("Notificaciones cargadas: " + notificationRows.size());
        } catch (Exception ex) {
            logger.accept("Error en notificaciones: " + ex.getMessage());
        }
    }

    public void markRead(Long notificationId) {
        if (notificationId == null) {
            logger.accept("Selecciona una notificacion de la lista.");
            return;
        }
        try {
            api.put("/api/notifications/" + notificationId + "/read", Map.of());
            logger.accept("Notificacion marcada como leida.");
            load();
        } catch (Exception ex) {
            logger.accept("Error al actualizar notificacion: " + ex.getMessage());
        }
    }

    public void markReadSilently(Long notificationId) {
        if (notificationId == null) {
            return;
        }
        try {
            api.put("/api/notifications/" + notificationId + "/read", Map.of());
            load();
        } catch (Exception ex) {
            logger.accept("Error al actualizar notificacion: " + ex.getMessage());
        }
    }

    public void markAllRead() {
        try {
            api.put("/api/notifications/me/read-all", Map.of());
            logger.accept("Todas las notificaciones marcadas como leidas");
            load();
        } catch (Exception ex) {
            logger.accept("Error en marcado masivo de notificaciones: " + ex.getMessage());
        }
    }
}
