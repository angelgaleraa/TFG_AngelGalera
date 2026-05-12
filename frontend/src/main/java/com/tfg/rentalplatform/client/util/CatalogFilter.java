package com.tfg.rentalplatform.client.util;

import com.tfg.rentalplatform.client.model.ItemRow;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static com.tfg.rentalplatform.client.util.CatalogOptions.displayCategory;
import static com.tfg.rentalplatform.client.util.LocationUtils.locationKey;
import static com.tfg.rentalplatform.client.util.LocationUtils.normalizeSearchText;

public final class CatalogFilter {

    private CatalogFilter() {
    }

    public static boolean hasFilters(String query, String category, String city, String municipality) {
        return hasText(query) || hasText(category) || hasText(city) || hasText(municipality);
    }

    public static List<ItemRow> filter(
            List<ItemRow> source,
            String query,
            String category,
            String city,
            String municipality,
            String sort
    ) {
        java.util.stream.Stream<ItemRow> rows = source.stream()
                .filter(item -> !item.moderationBlocked() && item.active());
        if (hasText(query)) {
            String queryKey = normalizeSearchText(query);
            rows = rows.filter(item -> normalizeSearchText(item.title()).contains(queryKey)
                    || normalizeSearchText(item.description()).contains(queryKey));
        }
        if (hasText(category)) {
            String categoryKey = normalizeSearchText(category);
            rows = rows.filter(item -> normalizeSearchText(displayCategory(item.category())).equals(categoryKey)
                    || normalizeSearchText(item.category()).equals(categoryKey));
        }
        if (hasText(city)) {
            String cityKey = locationKey(city);
            rows = rows.filter(item -> cityKey.equals(locationKey(item.city())));
        }
        if (hasText(municipality)) {
            String municipalityKey = locationKey(municipality);
            rows = rows.filter(item -> municipalityKey.equals(locationKey(item.municipality())));
        }
        List<ItemRow> result = rows.toList();
        if ("priceAsc".equals(sort)) {
            return result.stream().sorted(Comparator.comparing(CatalogFilter::priceValue)).toList();
        }
        if ("priceDesc".equals(sort)) {
            return result.stream().sorted(Comparator.comparing(CatalogFilter::priceValue).reversed()).toList();
        }
        return result;
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static BigDecimal priceValue(ItemRow item) {
        try {
            return new BigDecimal(item.pricePerDay());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}
