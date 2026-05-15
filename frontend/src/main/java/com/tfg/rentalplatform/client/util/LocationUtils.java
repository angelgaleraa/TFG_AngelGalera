package com.tfg.rentalplatform.client.util;

import java.text.Normalizer;
import java.util.Locale;

public final class LocationUtils {

    private LocationUtils() {
    }

    public static String normalizeLocationText(String value) {
        return Normalizer.normalize(TextUtils.fixCorruptedWords(TextUtils.fixMojibake(value == null ? "" : value)), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    public static String normalizeSearchText(String value) {
        return normalizeLocationText(value == null ? "" : value);
    }

    public static String locationKey(String value) {
        String key = normalizeLocationText(value).replaceAll("[^a-z0-9]", "");
        return switch (key) {
            case "mlaga" -> "malaga";
            case "valncia" -> "valencia";
            case "crdoba" -> "cordoba";
            case "cdiz" -> "cadiz";
            case "len" -> "leon";
            default -> key;
        };
    }

    public static String displayLocationText(Object value) {
        String text = TextUtils.fixCorruptedWords(TextUtils.fixMojibake(TextUtils.nullableString(value))).trim();
        int bilingualSeparator = text.indexOf('/');
        if (bilingualSeparator > 0) {
            text = text.substring(0, bilingualSeparator).trim();
        }
        return switch (locationKey(text)) {
            case "mlaga", "malaga" -> "Malaga";
            case "valncia", "valencia" -> "Valencia";
            case "crdoba", "cordoba" -> "Cordoba";
            case "cdiz", "cadiz" -> "Cadiz";
            case "avila" -> "Avila";
            case "araba", "lava", "alava" -> "Alava";
            case "len", "leon" -> "Leon";
            default -> Normalizer.normalize(text, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replace("?", "")
                    .trim();
        };
    }
}
