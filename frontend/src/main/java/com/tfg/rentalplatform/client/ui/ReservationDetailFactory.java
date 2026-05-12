package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.tfg.rentalplatform.client.ui.UiFactory.*;
import static com.tfg.rentalplatform.client.util.FormatUtils.formatMoney;
import static com.tfg.rentalplatform.client.util.FormatUtils.formatReservationDates;
import static com.tfg.rentalplatform.client.util.StatusText.reservationLabel;
import static com.tfg.rentalplatform.client.util.StatusText.reservationStyle;

public final class ReservationDetailFactory {

    private ReservationDetailFactory() {
    }

    public static DetailContent create(
            Map<String, Object> reservation,
            Function<Map<String, Object>, String> contactText,
            ReservationActions actions
    ) {
        if (reservation == null) {
            VBox empty = new VBox(6,
                    new Label("Sin reserva seleccionada"),
                    reservationMetaLabel("Elige una tarjeta de la lista para gestionar la solicitud.")
            );
            empty.getStyleClass().add("reservation-detail-summary");
            return new DetailContent("Selecciona una reserva para ver acciones y detalles.", List.of(empty));
        }

        Label item = new Label(String.valueOf(reservation.get("itemTitle")));
        item.getStyleClass().add("reservation-detail-title");
        Label dates = reservationMetaLabel(formatReservationDates(reservation));
        Label contact = reservationMetaLabel(contactText.apply(reservation));
        Label total = new Label(formatMoney(reservation.get("totalPrice")));
        total.getStyleClass().add("reservation-detail-total");
        String reservationStatus = String.valueOf(reservation.get("status"));
        Label status = badge(reservationLabel(reservationStatus));
        status.getStyleClass().add(reservationStyle(reservationStatus));

        VBox summary = new VBox(8, item, contact, dates, total, status);
        summary.getStyleClass().add("reservation-detail-summary");

        List<Node> nodes = new ArrayList<>();
        nodes.add(summary);
        VBox actionBox = buildActions(reservation, actions);
        if (!actionBox.getChildren().isEmpty()) {
            nodes.add(actionBox);
        }
        return new DetailContent("Reserva " + reservation.get("id"), nodes);
    }

    private static VBox buildActions(Map<String, Object> reservation, ReservationActions actions) {
        VBox wrapper = new VBox(12);
        wrapper.getStyleClass().add("reservation-actions");

        String type = String.valueOf(reservation.get("_type"));
        String status = String.valueOf(reservation.get("status"));
        HBox mainActions = new HBox(10);
        mainActions.getStyleClass().add("reservation-actions-row");
        HBox secondaryActions = new HBox(10);
        secondaryActions.getStyleClass().add("reservation-actions-row");

        if ("Propietario".equals(type)) {
            if ("PENDING".equals(status)) {
                mainActions.getChildren().add(actionButton("Aceptar", "primary-btn", actions.accept()));
                mainActions.getChildren().add(actionButton("Rechazar", null, actions.reject()));
            }
            if ("ACCEPTED".equals(status)) {
                mainActions.getChildren().add(actionButton("Completar", "primary-btn", actions.complete()));
                mainActions.getChildren().add(actionButton("Cancelar reserva", "danger-btn", actions.cancel()));
            }
            mainActions.getChildren().add(actionButton("Abrir chat", null, actions.openChat()));
        } else {
            if ("COMPLETED".equals(status)) {
                mainActions.getChildren().add(actionButton("Valorar propietario", "primary-btn", actions.review()));
            }
            if ("PENDING".equals(status) || "ACCEPTED".equals(status)) {
                mainActions.getChildren().add(actionButton("Cancelar reserva", "danger-btn", actions.cancel()));
            }
            mainActions.getChildren().add(actionButton("Abrir chat", null, actions.openChat()));
        }

        if ("COMPLETED".equals(status) || "REJECTED".equals(status) || "CANCELED".equals(status)) {
            secondaryActions.getChildren().add(actionButton("Quitar de mi lista", "danger-link-btn", actions.hideFromList()));
        }
        if (!mainActions.getChildren().isEmpty()) {
            wrapper.getChildren().add(mainActions);
        }
        if (!secondaryActions.getChildren().isEmpty()) {
            Separator separator = new Separator();
            separator.getStyleClass().add("reservation-actions-separator");
            wrapper.getChildren().addAll(separator, secondaryActions);
        }
        return wrapper;
    }

    public record DetailContent(String labelText, List<Node> nodes) {
    }

    public record ReservationActions(
            Runnable accept,
            Runnable reject,
            Runnable complete,
            Runnable cancel,
            Runnable review,
            Runnable openChat,
            Runnable hideFromList
    ) {
    }
}
