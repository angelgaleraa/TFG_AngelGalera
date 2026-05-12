package com.tfg.rentalplatform.client.util;

import com.tfg.rentalplatform.client.model.ItemRow;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class BookingSummary {

    private static final double PLATFORM_FEE_PERCENT = 10.0;

    private BookingSummary() {
    }

    public static void update(ItemRow item, LocalDate start, LocalDate end, Label totalAmount, Label totalBreakdown, Label totalNote) {
        if (start == null || end == null) {
            totalAmount.setText("-- EUR");
            totalBreakdown.setText("Selecciona fechas para calcular el total.");
            totalNote.setText("La solicitud se confirma cuando el propietario acepta.");
            return;
        }
        if (!end.isAfter(start)) {
            totalAmount.setText("-- EUR");
            totalBreakdown.setText("La fecha de fin debe ser posterior a la de inicio.");
            totalNote.setText("Revisa las fechas para continuar.");
            return;
        }
        long days = ChronoUnit.DAYS.between(start, end);
        double price = parsePrice(item.pricePerDay());
        double base = days * price;
        double fee = base * PLATFORM_FEE_PERCENT / 100.0;
        double total = base + fee;
        totalAmount.setText(String.format("%.2f EUR", total));
        totalBreakdown.setText(days + " dia" + (days == 1 ? "" : "s") + " x " + item.pricePerDay()
                + " EUR/dia + servicio " + String.format("%.2f EUR", fee));
        totalNote.setText("No se realiza ningun cargo hasta que se acepte la reserva.");
    }

    private static double parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
