package com.tfg.rentalplatform.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationTextTest {

    @Test
    void simplifiesReservationMessages() {
        assertEquals(
                "Nueva solicitud para 'Bicicleta urbana'",
                NotificationText.normalizeMessage("RESERVATION_CREATED", "Tienes una nueva solicitud de reserva para 'Bicicleta urbana'")
        );
        assertEquals(
                "Reserva cancelada",
                NotificationText.normalizeMessage("RESERVATION_CANCELED", "Reserva cancelada: reserva.")
        );
    }

    @Test
    void simplifiesPaymentMessages() {
        assertEquals(
                "Ingreso recibido por 'Altavoz JBL'",
                NotificationText.normalizeMessage("PAYMENT_CAPTURED", "Has recibido un pago simulado para 'Altavoz JBL'")
        );
        assertEquals(
                "Reembolso recibido por 'Camara'",
                NotificationText.normalizeMessage("PAYMENT_REFUNDED", "Reembolso completado para 'Camara'")
        );
    }

    @Test
    void returnsUsefulFallbackForBlankMessages() {
        assertEquals("Nueva actividad en tu cuenta.", NotificationText.normalizeMessage("ADMIN_ALERT", " "));
    }
}
