package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ItemDtos;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ItemDtos.PagedItemsResponse listActive(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String municipality,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return itemService.search(new ItemDtos.SearchRequest(query, category, city, municipality, minPrice, maxPrice, sort, page, size));
    }

    @GetMapping("/{itemId}")
    public ItemDtos.ItemResponse byId(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping("/me")
    public List<ItemDtos.ItemResponse> mine() {
        AuthenticatedUser currentUser = securityUtils.currentUser();
        return itemService.findByOwner(currentUser.id());
    }

    @PostMapping
    public ItemDtos.ItemResponse create(@Valid @RequestBody ItemDtos.CreateItemRequest request) {
        return itemService.create(securityUtils.currentUser(), request);
    }

    @PutMapping("/{itemId}")
    public ItemDtos.ItemResponse update(@PathVariable Long itemId, @Valid @RequestBody ItemDtos.UpdateItemRequest request) {
        return itemService.update(itemId, securityUtils.currentUser(), request);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        itemService.delete(itemId, securityUtils.currentUser());
    }
}
