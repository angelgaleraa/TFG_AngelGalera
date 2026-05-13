package com.tfg.rentalplatform.client.util;

import java.util.List;
import java.util.Map;

public final class AdminReportSearch {

    private AdminReportSearch() {
    }

    public static List<Map<String, Object>> filterReports(List<Map<String, Object>> reports, String query) {
        if (query == null || query.isBlank()) {
            return List.copyOf(reports);
        }
        String queryKey = LocationUtils.normalizeSearchText(query);
        return reports.stream()
                .filter(report -> LocationUtils.normalizeSearchText(searchText(report)).contains(queryKey))
                .toList();
    }

    public static String searchText(Map<String, Object> report) {
        return String.join(" ",
                TextUtils.cleanDisplayText(report.get("itemTitle")),
                TextUtils.cleanDisplayText(report.get("userName")),
                TextUtils.cleanDisplayText(report.get("reporterName")),
                TextUtils.cleanDisplayText(report.get("itemOwnerName")),
                TextUtils.cleanDisplayText(report.get("reason")),
                TextUtils.cleanDisplayText(report.get("details")),
                StatusText.reportLabel(report.get("status")),
                "ITEM".equals(String.valueOf(report.get("type"))) ? "Producto" : "Usuario"
        );
    }

    public static List<String> suggestionValues(List<Map<String, Object>> reports) {
        return reports.stream()
                .flatMap(report -> java.util.stream.Stream.of(
                        TextUtils.cleanDisplayText(report.get("itemTitle")),
                        TextUtils.cleanDisplayText(report.get("userName")),
                        TextUtils.cleanDisplayText(report.get("reporterName")),
                        TextUtils.cleanDisplayText(report.get("itemOwnerName")),
                        TextUtils.cleanDisplayText(report.get("reason"))
                ))
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public static List<String> matchingSuggestions(List<Map<String, Object>> reports, String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String queryKey = LocationUtils.normalizeSearchText(query);
        return suggestionValues(reports).stream()
                .filter(value -> LocationUtils.normalizeSearchText(value).contains(queryKey))
                .limit(limit)
                .toList();
    }
}
