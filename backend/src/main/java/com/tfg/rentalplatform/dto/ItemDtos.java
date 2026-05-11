package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Item;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ItemDtos {

    public record CreateItemRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotBlank @Size(max = 120) String category,
            @NotNull @DecimalMin("0.01") BigDecimal pricePerDay,
            @NotBlank @Size(max = 120) String city,
            @NotBlank @Size(max = 120) String municipality,
            String imageUrl
    ) {}

    public record UpdateItemRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotBlank @Size(max = 120) String category,
            @NotNull @DecimalMin("0.01") BigDecimal pricePerDay,
            @NotBlank @Size(max = 120) String city,
            @NotBlank @Size(max = 120) String municipality,
            String imageUrl,
            @NotNull Boolean active
    ) {}

    public record SearchRequest(
            String query,
            String category,
            String city,
            String municipality,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size
    ) {}

    public record PagedItemsResponse(
            List<ItemResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record ItemResponse(
            Long id,
            Long ownerId,
            String ownerName,
            String title,
            String description,
            String category,
            BigDecimal pricePerDay,
            String city,
            String municipality,
            String imageUrl,
            Boolean active,
            Boolean moderationBlocked,
            LocalDateTime createdAt
    ) {
        public static ItemResponse from(Item item) {
            return new ItemResponse(
                    item.getId(),
                    item.getOwner().getId(),
                    item.getOwner().getName(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getCategory(),
                    item.getPricePerDay(),
                    item.getCity(),
                    item.getMunicipality(),
                    item.getImageUrl(),
                    item.getActive(),
                    item.getModerationBlocked(),
                    item.getCreatedAt()
            );
        }
    }
}
