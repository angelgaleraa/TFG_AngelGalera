package com.tfg.rentalplatform.client.util;

import java.util.Locale;

public final class FeedbackText {

    private FeedbackText() {
    }

    public static String loginMessage(String error) {
        String lower = error == null ? "" : error.toLowerCase(Locale.ROOT);
        if (lower.contains("bloquead") || lower.contains("blocked") || lower.contains("disabled")) {
            return "Tu cuenta ha sido bloqueada. Contacta con soporte si crees que es un error.";
        }
        if (lower.contains("credenciales") || lower.contains("bad credentials") || lower.contains("authentication required")
                || lower.contains("unauthorized")) {
            return "Email o contrase\u00f1a incorrectos.";
        }
        return "No se pudo iniciar sesi\u00f3n. Int\u00e9ntalo de nuevo.";
    }

    public static String registerMessage(String error) {
        String lower = error == null ? "" : error.toLowerCase(Locale.ROOT);
        if (lower.contains("registrado") || lower.contains("in use") || lower.contains("already")) {
            return "Ese email ya est\u00e1 registrado.";
        }
        if (lower.contains("password") || lower.contains("contrase")) {
            return "La contrase\u00f1a no cumple los requisitos.";
        }
        if (lower.contains("email")) {
            return "Introduce un email v\u00e1lido.";
        }
        return "No se pudo crear la cuenta. Revisa los datos e int\u00e9ntalo de nuevo.";
    }

    public static boolean shouldShowUserFeedback(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("cargad") || lower.contains("conectado") || lower.contains("autenticaci")) {
            return false;
        }
        return lower.startsWith("error")
                || lower.contains("selecciona")
                || lower.contains("revisa")
                || lower.contains("necesitas")
                || lower.contains("completa")
                || lower.contains("correctamente")
                || lower.contains("guardados")
                || lower.contains("no puedes")
                || lower.contains("reserva")
                || lower.contains("reporte")
                || lower.contains("perfil")
                || lower.contains("contrase")
                || lower.contains("objeto")
                || lower.contains("usuario")
                || lower.contains("rese")
                || lower.contains("notificaci")
                || lower.contains("pago")
                || lower.contains("eliminado")
                || lower.contains("actualizado")
                || lower.contains("bloqueado")
                || lower.contains("desbloqueado")
                || lower.contains("sesion cerrada")
                || lower.contains("sesi");
    }

    public static String userFeedbackMessage(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.startsWith("error al crear objeto")) {
            return "No se pudo publicar el objeto. Revisa los datos e intentalo de nuevo.";
        }
        if (lower.startsWith("error al actualizar objeto")) {
            return "No se pudieron guardar los cambios del objeto.";
        }
        if (lower.startsWith("error al eliminar objeto")) {
            return "No se pudo eliminar el objeto.";
        }
        if (lower.startsWith("error en reserva")) {
            return "No se pudo completar la reserva.";
        }
        if (lower.startsWith("error en accion de reserva") || lower.startsWith("error en acci")) {
            return "No se pudo actualizar la reserva.";
        }
        if (lower.startsWith("error al quitar reserva")) {
            return "No se pudo quitar la reserva de la lista.";
        }
        if (lower.startsWith("error al actualizar notificaci")) {
            return "No se pudo actualizar la notificaci\u00f3n.";
        }
        if (lower.startsWith("error al enviar reporte")) {
            return "No se pudo enviar el reporte.";
        }
        if (lower.startsWith("error al guardar rese")) {
            return "No se pudo guardar la rese\u00f1a.";
        }
        if (lower.startsWith("error al actualizar perfil")) {
            return "No se pudo actualizar el perfil.";
        }
        if (lower.startsWith("error al cambiar contrase")) {
            return "No se pudo actualizar la contrase\u00f1a.";
        }
        return message;
    }
}
