package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ItemDtos;
import com.tfg.rentalplatform.entity.Item;
import com.tfg.rentalplatform.entity.ReservationStatus;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ItemDtos.ItemResponse create(AuthenticatedUser currentUser, ItemDtos.CreateItemRequest request) {
        User owner = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario propietario no encontrado"));

        Item item = new Item();
        item.setOwner(owner);
        item.setTitle(request.title().trim());
        item.setDescription(request.description().trim());
        item.setCategory(request.category().trim());
        item.setPricePerDay(request.pricePerDay());
        item.setCity(request.city().trim());
        item.setMunicipality(request.municipality().trim());
        item.setImageUrl(request.imageUrl());
        item.setActive(true);
        item.setModerationBlocked(false);
        item.setOwnerRemoved(false);
        item.setUpdatedAt(LocalDateTime.now());
        return ItemDtos.ItemResponse.from(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemDtos.PagedItemsResponse search(ItemDtos.SearchRequest request) {
        int page = Math.max(request.page(), 0);
        int size = request.size() <= 0 ? 10 : Math.min(request.size(), 100);

        Specification<Item> spec = Specification.where(visibleInCatalog());
        if (hasText(request.query())) {
            spec = spec.and(titleOrDescriptionLike(request.query()));
        }
        if (hasText(request.category())) {
            spec = spec.and(categoryEq(request.category()));
        }
        if (hasText(request.city())) {
            spec = spec.and(cityEq(request.city()));
        }
        if (hasText(request.municipality())) {
            spec = spec.and(municipalityEq(request.municipality()));
        }
        if (request.minPrice() != null) {
            spec = spec.and(minPrice(request.minPrice()));
        }
        if (request.maxPrice() != null) {
            spec = spec.and(maxPrice(request.maxPrice()));
        }

        Page<Item> result = itemRepository.findAll(spec, PageRequest.of(page, size, catalogSort(request.sort())));
        return new ItemDtos.PagedItemsResponse(
                result.getContent().stream().map(ItemDtos.ItemResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<ItemDtos.ItemResponse> findByOwner(Long ownerId) {
        return itemRepository.findByOwnerIdAndOwnerRemovedFalseOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(ItemDtos.ItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemDtos.ItemResponse getById(Long id) {
        return ItemDtos.ItemResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Item getEntity(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> ApiException.notFound("ITEM_NOT_FOUND", "Objeto no encontrado"));
    }

    @Transactional
    public ItemDtos.ItemResponse update(Long itemId, AuthenticatedUser currentUser, ItemDtos.UpdateItemRequest request) {
        Item item = getEntity(itemId);
        if (!item.getOwner().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("ITEM_NOT_OWNER", "No puedes editar este objeto");
        }
        item.setTitle(request.title().trim());
        item.setDescription(request.description().trim());
        item.setCategory(request.category().trim());
        item.setPricePerDay(request.pricePerDay());
        item.setCity(request.city().trim());
        item.setMunicipality(request.municipality().trim());
        item.setImageUrl(request.imageUrl());
        if (Boolean.TRUE.equals(item.getModerationBlocked()) && Boolean.TRUE.equals(request.active())) {
            throw ApiException.forbidden("ITEM_BLOCKED_BY_ADMIN", "Este objeto ha sido retirado por administración");
        }
        item.setActive(request.active());
        item.setUpdatedAt(LocalDateTime.now());
        return ItemDtos.ItemResponse.from(item);
    }

    @Transactional
    public ItemDtos.ItemResponse setActive(Long itemId, Boolean active) {
        Item item = getEntity(itemId);
        if (Boolean.TRUE.equals(item.getModerationBlocked()) && Boolean.TRUE.equals(active)) {
            throw ApiException.forbidden("ITEM_BLOCKED_BY_ADMIN", "Este objeto ha sido retirado por administración");
        }
        item.setActive(active);
        item.setUpdatedAt(LocalDateTime.now());
        return ItemDtos.ItemResponse.from(item);
    }

    @Transactional
    public ItemDtos.ItemResponse blockByModeration(Long itemId) {
        Item item = getEntity(itemId);
        item.setActive(false);
        item.setModerationBlocked(true);
        item.setUpdatedAt(LocalDateTime.now());
        return ItemDtos.ItemResponse.from(item);
    }

    @Transactional
    public ItemDtos.ItemResponse unblockByModeration(Long itemId) {
        Item item = getEntity(itemId);
        item.setActive(true);
        item.setModerationBlocked(false);
        item.setUpdatedAt(LocalDateTime.now());
        return ItemDtos.ItemResponse.from(item);
    }

    @Transactional
    public void delete(Long itemId, AuthenticatedUser currentUser) {
        Item item = getEntity(itemId);
        if (!item.getOwner().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("ITEM_NOT_OWNER", "No puedes eliminar este objeto");
        }
        if (reservationRepository.existsByItemIdAndStatusIn(itemId, List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED))) {
            throw ApiException.badRequest(
                    "ITEM_HAS_OPEN_RESERVATIONS",
                    "No puedes eliminar este objeto mientras tenga reservas pendientes o aceptadas"
            );
        }
        item.setActive(false);
        item.setOwnerRemoved(true);
        item.setUpdatedAt(LocalDateTime.now());
    }

    private Specification<Item> visibleInCatalog() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("active"), true),
                cb.equal(root.get("moderationBlocked"), false),
                cb.equal(root.get("ownerRemoved"), false),
                cb.equal(root.get("owner").get("active"), true)
        );
    }

    private Specification<Item> titleOrDescriptionLike(String query) {
        return (root, cq, cb) -> {
            String value = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), value),
                    cb.like(cb.lower(root.get("description")), value)
            );
        };
    }

    private Specification<Item> categoryEq(String category) {
        return textEqIgnoringAccents("category", category);
    }

    private Specification<Item> cityEq(String city) {
        return textEqIgnoringAccents("city", city);
    }

    private Specification<Item> municipalityEq(String municipality) {
        return textEqIgnoringAccents("municipality", municipality);
    }

    private Specification<Item> textEqIgnoringAccents(String field, String value) {
        return (root, query, cb) -> {
            String lower = value.trim().toLowerCase();
            String unaccented = stripAccents(lower);
            java.util.Set<String> candidates = new java.util.LinkedHashSet<>();
            candidates.add(lower);
            candidates.add(unaccented);
            candidates.addAll(locationAliases(unaccented));

            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            for (String candidate : candidates) {
                predicates.add(cb.equal(cb.lower(root.get(field)), candidate));
                predicates.add(cb.like(cb.lower(root.get(field)), candidate + "/%"));
                predicates.add(cb.like(cb.lower(root.get(field)), candidate + "%"));
            }
            return cb.or(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private List<String> locationAliases(String value) {
        return switch (value) {
            case "malaga" -> List.of("m?laga", "m laga", "mlaga");
            case "valencia" -> List.of("val?ncia", "val ncia", "valncia", "val\u00e3\u00a8ncia", "val\u00c3\u00a8ncia");
            case "cordoba" -> List.of("c?rdoba", "c rdoba", "crdoba");
            case "cadiz" -> List.of("c?diz", "c diz", "cdiz");
            case "leon" -> List.of("le?n", "le n", "len");
            default -> List.of();
        };
    }

    private Specification<Item> minPrice(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("pricePerDay"), min);
    }

    private Specification<Item> maxPrice(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("pricePerDay"), max);
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private Sort catalogSort(String sort) {
        if ("priceAsc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "pricePerDay");
        }
        if ("priceDesc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "pricePerDay");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
