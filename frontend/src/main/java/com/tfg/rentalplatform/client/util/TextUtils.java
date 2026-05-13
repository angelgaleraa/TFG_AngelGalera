package com.tfg.rentalplatform.client.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String nullableString(Object value) {
        if (value == null || "null".equalsIgnoreCase(String.valueOf(value))) {
            return "";
        }
        return String.valueOf(value);
    }

    public static String wrapLongWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        StringBuilder wrapped = new StringBuilder(text.length() + 16);
        int currentTokenLength = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            wrapped.append(ch);
            if (Character.isWhitespace(ch)) {
                currentTokenLength = 0;
                continue;
            }
            currentTokenLength++;
            if (currentTokenLength >= 34) {
                wrapped.append('\u200B');
                currentTokenLength = 0;
            }
        }
        return wrapped.toString();
    }

    public static String previewText(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return "Sin descripcion disponible.";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars - 1)).trim() + "...";
    }

    public static String fixMojibake(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return text
                .replace("administraciÃ³n", "administracion")
                .replace("AdministraciÃ³n", "Administracion")
                .replace("revisiÃ³n", "revision")
                .replace("RevisiÃ³n", "Revision")
                .replace("notificaciÃ³n", "notificacion")
                .replace("NotificaciÃ³n", "Notificacion")
                .replace("contraseÃ±a", "contrasena")
                .replace("ContraseÃ±a", "Contrasena")
                .replace("reseÃ±a", "resena")
                .replace("ReseÃ±a", "Resena")
                .replace("LeÃ­da", "Leida")
                .replace("Â·", "-");
    }

    public static String cleanDisplayText(Object value) {
        return fixCorruptedWords(fixMojibake(nullableString(value)))
                .replace("Â·", "-")
                .trim();
    }

    public static String fixCorruptedWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        text = text
                .replace("\u00c3\u201a\u00c2\u00b7", "-")
                .replace("\u00c3\u201a-", "-");
        return text
                .replaceAll("(?i)enga[^\\p{Alnum}\\s]{1,4}oso", "enga\u00f1oso")
                .replaceAll("(?i)a[^\\p{Alnum}\\s]{1,4}ade", "a\u00f1ade")
                .replaceAll("(?i)administraci[^\\p{Alnum}\\s]{1,4}n", "administracion")
                .replaceAll("(?i)informaci[^\\p{Alnum}\\s]{1,4}n", "informacion")
                .replaceAll("(?i)descripci[^\\p{Alnum}\\s]{1,4}n", "descripcion")
                .replaceAll("(?i)revisi[^\\p{Alnum}\\s]{1,4}n", "revision")
                .replaceAll("(?i)notificaci[^\\p{Alnum}\\s]{1,4}n", "notificacion")
                .replaceAll("(?i)contrase[^\\p{Alnum}\\s]{1,4}a", "contrasena")
                .replaceAll("(?i)rese[^\\p{Alnum}\\s]{1,4}a", "resena")
                .replaceAll("(?i)categor[^\\p{Alnum}\\s]{1,4}a", "categoria")
                .replaceAll("(?i)electr[^\\p{Alnum}\\s]{1,4}nica", "electronica")
                .replaceAll("(?i)sesi[^\\p{Alnum}\\s]{1,4}n", "sesion")
                .replaceAll("(?i)cat[^\\p{Alnum}\\s]{1,4}logo", "catalogo");
    }
}
