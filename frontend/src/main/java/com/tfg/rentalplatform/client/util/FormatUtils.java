package com.tfg.rentalplatform.client.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class FormatUtils {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtils() {
    }

    public static Long toLongOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Long toLong(Object value) {
        return ((Number) value).longValue();
    }

    public static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static double parseMoney(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(value).replace(",", "."));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static String formatDateTime(Object value) {
        String text = TextUtils.nullableString(value);
        if (text.isBlank()) {
            return "";
        }
        text = text.replace('T', ' ');
        int dot = text.indexOf('.');
        if (dot > 0) {
            text = text.substring(0, dot);
        }
        return text;
    }

    public static String formatReservationDates(Map<String, Object> reservation) {
        String start = formatDate(reservation.get("startDate"));
        String end = formatDate(reservation.get("endDate"));
        if (start.isBlank() && end.isBlank()) {
            return "";
        }
        if (end.isBlank()) {
            return "Desde " + start;
        }
        if (start.isBlank()) {
            return "Hasta " + end;
        }
        return "Del " + start + " al " + end;
    }

    public static String formatDate(Object value) {
        String text = TextUtils.nullableString(value);
        if (text.isBlank()) {
            return "";
        }
        try {
            return LocalDate.parse(text).format(DISPLAY_DATE_FORMAT);
        } catch (Exception ex) {
            return text;
        }
    }

    public static String formatMoney(Object value) {
        if (value instanceof Number number) {
            return String.format(Locale.ROOT, "%.2f EUR", number.doubleValue());
        }
        String text = TextUtils.nullableString(value);
        if (text.isBlank()) {
            return "-- EUR";
        }
        try {
            return String.format(Locale.ROOT, "%.2f EUR", Double.parseDouble(text.replace(",", ".")));
        } catch (NumberFormatException ex) {
            return text + " EUR";
        }
    }
}
