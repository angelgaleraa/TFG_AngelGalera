package com.tfg.rentalplatform.client.mapper;

import com.tfg.rentalplatform.client.model.ItemRow;
import com.tfg.rentalplatform.client.util.FormatUtils;
import com.tfg.rentalplatform.client.util.LocationUtils;

import java.util.Map;

public final class ItemMapper {

    private ItemMapper() {
    }

    public static ItemRow toItemRow(Map<String, Object> map) {
        return new ItemRow(
                FormatUtils.toLong(map.get("id")),
                FormatUtils.toLong(map.get("ownerId")),
                String.valueOf(map.get("title")),
                String.valueOf(map.get("description")),
                String.valueOf(map.get("category")),
                LocationUtils.displayLocationText(map.get("city")),
                LocationUtils.displayLocationText(map.getOrDefault("municipality", map.get("city"))),
                String.valueOf(map.get("pricePerDay")),
                String.valueOf(map.get("ownerName")),
                String.valueOf(map.getOrDefault("imageUrl", "")),
                Boolean.parseBoolean(String.valueOf(map.get("active"))),
                Boolean.parseBoolean(String.valueOf(map.getOrDefault("moderationBlocked", false)))
        );
    }
}
