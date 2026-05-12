package com.tfg.rentalplatform.client.util;

public final class StatusText {

    private StatusText() {
    }

    public static String reservationLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Pendiente";
            case "ACCEPTED" -> "Aceptada";
            case "REJECTED" -> "Rechazada";
            case "CANCELED" -> "Cancelada";
            case "COMPLETED" -> "Completada";
            default -> status == null ? "" : status;
        };
    }

    public static String reservationStyle(String status) {
        return switch (status) {
            case "ACCEPTED" -> "badge-success";
            case "REJECTED", "CANCELED" -> "badge-muted";
            case "COMPLETED" -> "badge-completed";
            default -> "badge-pending";
        };
    }

    public static String reservationActionMessage(String action) {
        return switch (action) {
            case "accept" -> "Reserva aceptada.";
            case "reject" -> "Reserva rechazada.";
            case "complete" -> "Reserva completada.";
            case "cancel" -> "Reserva cancelada.";
            default -> "Reserva actualizada.";
        };
    }

    public static String paymentLabel(String status, boolean ownerView) {
        return switch (status) {
            case "CAPTURED" -> ownerView ? "Ingresado" : "Pagado";
            case "REFUNDED" -> "Reembolsado";
            default -> status == null ? "" : status;
        };
    }

    public static String paymentMethodLabel(String method) {
        return switch (method) {
            case "SIMULATED_CARD" -> "Tarjeta simulada";
            default -> method == null || method.isBlank() || "null".equals(method) ? "Metodo de pago" : method;
        };
    }

    public static String paymentStyle(String status) {
        return switch (status) {
            case "CAPTURED" -> "badge-success";
            case "REFUNDED" -> "badge-muted";
            default -> "badge-pending";
        };
    }

    public static String notificationTypeLabel(String type) {
        return switch (type) {
            case "RESERVATION_CREATED", "RESERVATION_ACCEPTED", "RESERVATION_REJECTED",
                    "RESERVATION_CANCELED", "RESERVATION_COMPLETED" -> "Reserva";
            case "PAYMENT_CAPTURED", "PAYMENT_REFUNDED" -> "Pago";
            case "MESSAGE_RECEIVED" -> "Mensaje";
            case "REVIEW_CREATED" -> "Rese\u00f1a";
            case "ADMIN_ALERT" -> "Sistema";
            default -> "Actividad";
        };
    }

    public static String reportLabel(Object status) {
        return "REVIEWED".equals(String.valueOf(status)) ? "Cerrado" : "Abierto";
    }
}
