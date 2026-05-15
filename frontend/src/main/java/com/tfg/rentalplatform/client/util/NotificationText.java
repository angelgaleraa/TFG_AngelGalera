package com.tfg.rentalplatform.client.util;

public final class NotificationText {

    private NotificationText() {
    }

    public static String normalizeMessage(String type, String message) {
        if (message == null || message.isBlank()) {
            return "Nueva actividad en tu cuenta.";
        }
        String clean = TextUtils.fixCorruptedWords(TextUtils.fixMojibake(message))
                .replace("simulado ", "")
                .replace("marcado como ", "")
                .replaceAll("\\s+", " ")
                .trim();
        clean = clean.replaceAll("(?i)reserva #?\\d+", "reserva");
        return switch (type) {
            case "RESERVATION_CREATED" -> clean
                    .replace("Tienes una nueva solicitud de reserva para", "Nueva solicitud para")
                    .replace("Solicitud de reserva para", "Nueva solicitud para")
                    .replace("Reserva creada para", "Nueva solicitud para");
            case "RESERVATION_ACCEPTED" -> clean.replace("Tu reserva", "Reserva").replace(" ha sido aceptada", " aceptada");
            case "RESERVATION_REJECTED" -> clean.replace("Tu reserva", "Reserva").replace(" ha sido rechazada", " rechazada");
            case "RESERVATION_CANCELED" -> clean
                    .replace("La reserva", "Reserva")
                    .replace("Has cancelado la", "Reserva cancelada:")
                    .replaceAll("(?i)Reserva cancelada:? reserva\\.?", "Reserva cancelada");
            case "RESERVATION_COMPLETED" -> clean.replace("Tu reserva", "Reserva").replace(" ha sido completada", " completada");
            case "PAYMENT_CAPTURED" -> clean
                    .replace("Has recibido un pago para", "Ingreso recibido por")
                    .replace("Pago recibido por la reserva", "Ingreso recibido")
                    .replace("Pago recibido por", "Ingreso recibido por")
                    .replace("Pago capturado para", "Gasto registrado para")
                    .replaceAll("(?i)Ingreso recibido reserva\\.?", "Ingreso recibido");
            case "PAYMENT_REFUNDED" -> clean
                    .replace("Reembolso completado para", "Reembolso recibido por")
                    .replace("Pago reembolsado para", "Ingreso devuelto por")
                    .replaceAll("(?i)(Reembolso recibido|Ingreso devuelto) reserva\\.?", "$1");
            default -> clean;
        };
    }
}
