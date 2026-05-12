package com.tfg.rentalplatform.client.ui;

import com.tfg.rentalplatform.client.util.FormatUtils;
import com.tfg.rentalplatform.client.util.NotificationText;
import com.tfg.rentalplatform.client.util.StatusText;
import com.tfg.rentalplatform.client.util.TextUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

public final class ActivityRowFactory {

    private ActivityRowFactory() {
    }

    public static Node reservationRow(Map<String, Object> reservation, String contactText) {
        String reservationStatus = String.valueOf(reservation.get("status"));
        Label status = UiFactory.badge(StatusText.reservationLabel(reservationStatus));
        status.getStyleClass().add(StatusText.reservationStyle(reservationStatus));

        Label title = new Label(String.valueOf(reservation.get("itemTitle")));
        title.getStyleClass().add("reservation-row-title");

        Label reservationId = new Label("Reserva " + reservation.get("id"));
        reservationId.getStyleClass().add("reservation-row-id");

        Label dates = new Label(FormatUtils.formatReservationDates(reservation));
        dates.getStyleClass().add("reservation-row-dates");

        Label contact = new Label(contactText);
        contact.getStyleClass().add("reservation-row-dates");

        Label total = new Label(FormatUtils.formatMoney(reservation.get("totalPrice")));
        total.getStyleClass().add("reservation-row-total");

        VBox text = new VBox(6, reservationId, title, contact, dates);
        HBox.setHgrow(text, Priority.ALWAYS);
        VBox right = new VBox(6, total, status);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(14, text, right);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("reservation-row");
        return row;
    }

    public static Node paymentRow(Map<String, Object> payment, boolean ownerView) {
        Label title = new Label(TextUtils.nullableString(payment.get("itemTitle")).isBlank()
                ? "Reserva " + payment.get("reservationId")
                : TextUtils.nullableString(payment.get("itemTitle")));
        title.getStyleClass().add("payment-row-title");

        Label reservation = new Label("Reserva " + payment.get("reservationId"));
        reservation.getStyleClass().add("reservation-row-id");

        String datesText = FormatUtils.formatReservationDates(payment);
        Label meta = new Label(datesText.isBlank()
                ? StatusText.paymentMethodLabel(String.valueOf(payment.get("method")))
                : datesText + " - " + StatusText.paymentMethodLabel(String.valueOf(payment.get("method"))));
        meta.getStyleClass().add("payment-row-meta");
        VBox text = new VBox(6, reservation, title, meta);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label amount = new Label(FormatUtils.formatMoney(payment.get("amount")));
        amount.getStyleClass().add("payment-row-amount");
        String paymentStatus = String.valueOf(payment.get("status"));
        Label status = UiFactory.badge(StatusText.paymentLabel(paymentStatus, ownerView));
        status.getStyleClass().add(StatusText.paymentStyle(paymentStatus));
        VBox right = new VBox(6, amount, status);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(14, text, right);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("payment-row");
        return row;
    }

    public static Node notificationRow(Map<String, Object> notification) {
        boolean read = Boolean.TRUE.equals(notification.get("read"));
        String typeValue = String.valueOf(notification.get("type"));
        Label type = UiFactory.badge(StatusText.notificationTypeLabel(typeValue));
        type.getStyleClass().add("notification-type-badge");
        type.getStyleClass().add(read ? "badge-muted" : "badge-pending");
        type.setMinWidth(78);
        type.setPrefWidth(78);
        type.setAlignment(Pos.CENTER);
        type.setTextOverrun(OverrunStyle.CLIP);

        Label message = new Label(TextUtils.wrapLongWords(
                NotificationText.normalizeMessage(typeValue, String.valueOf(notification.get("message")))));
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.getStyleClass().add("notification-message");

        Label meta = new Label(read ? "Leida" : "Pendiente");
        meta.getStyleClass().add(read ? "notification-meta" : "notification-meta-unread");

        VBox text = new VBox(7, message, meta);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.setMaxWidth(Double.MAX_VALUE);
        HBox row = new HBox(12, type, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add(read ? "notification-row" : "notification-row-unread");
        return row;
    }
}
