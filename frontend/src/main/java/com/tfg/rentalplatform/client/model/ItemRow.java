package com.tfg.rentalplatform.client.model;

public record ItemRow(
        Long id,
        Long ownerId,
        String title,
        String description,
        String category,
        String city,
        String municipality,
        String pricePerDay,
        String ownerName,
        String imageUrl,
        boolean active,
        boolean moderationBlocked
) {
    public ItemRow withOwnerName(String newOwnerName) {
        return new ItemRow(id, ownerId, title, description, category, city, municipality, pricePerDay, newOwnerName, imageUrl, active, moderationBlocked);
    }
}
