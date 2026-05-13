package com.tfg.rentalplatform.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusTextTest {

    @Test
    void translatesReservationAndPaymentStatuses() {
        assertEquals("Aceptada", StatusText.reservationLabel("ACCEPTED"));
        assertEquals("badge-muted", StatusText.reservationStyle("CANCELED"));
        assertEquals("Reserva cancelada.", StatusText.reservationActionMessage("cancel"));
        assertEquals("Ingresado", StatusText.paymentLabel("CAPTURED", true));
        assertEquals("Pagado", StatusText.paymentLabel("CAPTURED", false));
        assertEquals("Tarjeta simulada", StatusText.paymentMethodLabel("SIMULATED_CARD"));
    }

    @Test
    void groupsNotificationAndReportLabels() {
        assertEquals("Reserva", StatusText.notificationTypeLabel("RESERVATION_CREATED"));
        assertEquals("Sistema", StatusText.notificationTypeLabel("ADMIN_ALERT"));
        assertEquals("Cerrado", StatusText.reportLabel("REVIEWED"));
        assertEquals("Abierto", StatusText.reportLabel("OPEN"));
    }
}
