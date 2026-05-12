package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.tfg.rentalplatform.client.util.FormatUtils.toLong;
import static com.tfg.rentalplatform.client.util.FormatUtils.toLongOrNull;
import static com.tfg.rentalplatform.client.util.StatusText.reservationActionMessage;

public class ReservationController {

    private final ApiClient api;
    private final ObservableList<String> reservations;
    private final BooleanSupplier loggedCheck;
    private final Function<Map<String, Object>, String> contactTextResolver;
    private final Runnable renderDetail;
    private final Runnable loadPayerPayments;
    private final Runnable loadNotifications;
    private final Consumer<String> logger;

    private final Map<Long, Map<String, Object>> rowsById = new HashMap<>();
    private Long selectedId;
    private Map<String, Object> selectedData;
    private String currentType = "Inquilino";

    public ReservationController(ApiClient api,
                                 ObservableList<String> reservations,
                                 BooleanSupplier loggedCheck,
                                 Function<Map<String, Object>, String> contactTextResolver,
                                 Runnable renderDetail,
                                 Runnable loadPayerPayments,
                                 Runnable loadNotifications,
                                 Consumer<String> logger) {
        this.api = api;
        this.reservations = reservations;
        this.loggedCheck = loggedCheck;
        this.contactTextResolver = contactTextResolver;
        this.renderDetail = renderDetail;
        this.loadPayerPayments = loadPayerPayments;
        this.loadNotifications = loadNotifications;
        this.logger = logger;
    }

    public Long selectedId() {
        return selectedId;
    }

    public Map<String, Object> selectedData() {
        return selectedData;
    }

    public String currentType() {
        return currentType;
    }

    public Map<String, Object> rowById(Long reservationId) {
        return reservationId == null ? null : rowsById.get(reservationId);
    }

    public void select(Long reservationId) {
        selectedId = reservationId;
        selectedData = selectedId == null ? null : rowsById.get(selectedId);
    }

    public void loadRenterReservations() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            List<Map<String, Object>> response = api.getList("/api/reservations/me");
            currentType = "Inquilino";
            fill(response, "Inquilino");
            logger.accept("Reservas de inquilino cargadas");
        } catch (Exception ex) {
            logger.accept("Error al cargar mis reservas: " + ex.getMessage());
        }
    }

    public void loadOwnerReservations() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            List<Map<String, Object>> response = api.getList("/api/reservations/owner");
            currentType = "Propietario";
            fill(response, "Propietario");
            logger.accept("Reservas de propietario cargadas");
        } catch (Exception ex) {
            logger.accept("Error al cargar reservas de propietario: " + ex.getMessage());
        }
    }

    public void action(String action) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (selectedId == null) {
            logger.accept("Selecciona una reserva de la lista.");
            return;
        }
        try {
            api.put("/api/reservations/" + selectedId + "/" + action, Map.of());
            logger.accept(reservationActionMessage(action));
            reloadCurrent();
            loadPayerPayments.run();
            loadNotifications.run();
        } catch (Exception ex) {
            logger.accept("Error en accion de reserva: " + ex.getMessage());
        }
    }

    public void reloadCurrent() {
        if ("Propietario".equals(currentType)) {
            loadOwnerReservations();
        } else {
            loadRenterReservations();
        }
    }

    public void hideSelectedFromList() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (selectedId == null) {
            logger.accept("Selecciona una reserva para quitarla de la lista.");
            return;
        }
        try {
            api.delete("/api/reservations/" + selectedId + "/list");
            logger.accept("Reserva quitada de tu lista.");
            reloadCurrent();
        } catch (Exception ex) {
            logger.accept("Error al quitar reserva: " + ex.getMessage());
        }
    }

    public void clear() {
        reservations.clear();
        rowsById.clear();
        selectedId = null;
        selectedData = null;
        currentType = "Inquilino";
    }

    public void syncCurrentUserName(Long currentUserId, String currentUserName) {
        rowsById.values().forEach(row -> {
            if (currentUserId.equals(toLongOrNull(row.get("renterId")))) {
                row.put("renterName", currentUserName);
            }
            if (currentUserId.equals(toLongOrNull(row.get("ownerId")))) {
                row.put("ownerName", currentUserName);
            }
        });
    }

    private void fill(List<Map<String, Object>> data, String type) {
        rowsById.clear();
        reservations.setAll(data.stream().map(rowData -> {
            Map<String, Object> row = new HashMap<>(rowData);
            row.put("_type", type);
            rowsById.put(toLong(rowData.get("id")), row);
            return type + " | #" + rowData.get("id")
                    + " | objeto=" + rowData.get("itemTitle")
                    + " | " + contactTextResolver.apply(row)
                    + " | " + rowData.get("startDate") + " -> " + rowData.get("endDate")
                    + " | estado=" + rowData.get("status")
                    + " | total=" + rowData.get("totalPrice");
        }).toList());
        selectedId = null;
        selectedData = null;
        renderDetail.run();
    }
}
