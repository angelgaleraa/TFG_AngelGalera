package com.tfg.rentalplatform.client.util;

import com.tfg.rentalplatform.client.model.ItemRow;

import java.util.Collection;
import java.util.Map;

import static com.tfg.rentalplatform.client.util.FormatUtils.toLongOrNull;
import static com.tfg.rentalplatform.client.util.TextUtils.cleanDisplayText;

public final class ReservationText {

    private ReservationText() {
    }

    @SafeVarargs
    public static String contactText(Map<String, Object> reservation, String currentReservationType, Collection<ItemRow>... itemSources) {
        String type = String.valueOf(reservation.getOrDefault("_type", currentReservationType));
        if ("Propietario".equals(type)) {
            String renterName = cleanDisplayText(reservation.get("renterName"));
            return renterName.isBlank() ? "Solicitada por usuario" : "Solicitada por " + renterName;
        }
        String ownerName = resolveOwnerName(reservation, itemSources);
        return ownerName.isBlank() ? "Propietario no disponible" : "Propietario: " + ownerName;
    }

    @SafeVarargs
    public static String resolveOwnerName(Map<String, Object> reservation, Collection<ItemRow>... itemSources) {
        String ownerName = cleanDisplayText(reservation.get("ownerName"));
        if (!ownerName.isBlank()) {
            return ownerName;
        }

        Long itemId = toLongOrNull(reservation.get("itemId"));
        Long ownerId = toLongOrNull(reservation.get("ownerId"));
        if (itemId == null && ownerId == null) {
            return "";
        }

        for (Collection<ItemRow> source : itemSources) {
            if (source == null) {
                continue;
            }
            for (ItemRow item : source) {
                if ((itemId != null && itemId.equals(item.id()))
                        || (ownerId != null && ownerId.equals(item.ownerId()))) {
                    String candidate = cleanDisplayText(item.ownerName());
                    if (!candidate.isBlank()) {
                        return candidate;
                    }
                }
            }
        }
        return "";
    }
}
