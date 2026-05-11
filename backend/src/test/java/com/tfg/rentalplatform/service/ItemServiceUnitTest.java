package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ItemDtos;
import com.tfg.rentalplatform.entity.Item;
import com.tfg.rentalplatform.entity.ReservationStatus;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks private ItemService itemService;

    @Test
    void createTrimsFieldsAndSetsDefaultVisibilityFlags() {
        User owner = user(1L, "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(20L);
            return item;
        });

        ItemDtos.ItemResponse response = itemService.create(currentUser(1L), new ItemDtos.CreateItemRequest(
                " Altavoz ",
                " Descripcion ",
                " Electronica ",
                new BigDecimal("18.00"),
                " Sevilla ",
                " Sevilla ",
                "https://example.com/altavoz.jpg"
        ));

        assertEquals(20L, response.id());
        assertEquals("Altavoz", response.title());
        assertEquals("Electronica", response.category());
        assertTrue(response.active());
        assertFalse(response.moderationBlocked());
    }

    @Test
    void updateDoesNotAllowOwnerToReactivateItemBlockedByAdmin() {
        Item item = item(5L, user(1L, "Ana"));
        item.setActive(false);
        item.setModerationBlocked(true);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        ApiException exception = assertThrows(ApiException.class, () -> itemService.update(
                5L,
                currentUser(1L),
                updateRequest(true)
        ));

        assertEquals("ITEM_BLOCKED_BY_ADMIN", exception.getCode());
    }

    @Test
    void deleteKeepsItemWhenItHasOpenReservations() {
        Item item = item(9L, user(1L, "Ana"));
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));
        when(reservationRepository.existsByItemIdAndStatusIn(9L, List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED)))
                .thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> itemService.delete(9L, currentUser(1L)));

        assertEquals("ITEM_HAS_OPEN_RESERVATIONS", exception.getCode());
        assertFalse(item.getOwnerRemoved());
        assertTrue(item.getActive());
    }

    @Test
    void deleteMarksOwnerItemAsRemovedWhenThereAreNoOpenReservations() {
        Item item = item(9L, user(1L, "Ana"));
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));

        itemService.delete(9L, currentUser(1L));

        assertTrue(item.getOwnerRemoved());
        assertFalse(item.getActive());
        verify(reservationRepository).existsByItemIdAndStatusIn(9L, List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED));
    }

    private AuthenticatedUser currentUser(Long id) {
        return new AuthenticatedUser(id, "ana@test.com", UserRole.USER);
    }

    private User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setActive(true);
        user.setRole(UserRole.USER);
        return user;
    }

    private Item item(Long id, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setOwner(owner);
        item.setTitle("Altavoz");
        item.setDescription("Descripcion");
        item.setCategory("Electronica");
        item.setPricePerDay(new BigDecimal("18.00"));
        item.setCity("Sevilla");
        item.setMunicipality("Sevilla");
        item.setActive(true);
        item.setModerationBlocked(false);
        item.setOwnerRemoved(false);
        return item;
    }

    private ItemDtos.UpdateItemRequest updateRequest(boolean active) {
        return new ItemDtos.UpdateItemRequest(
                "Altavoz",
                "Descripcion",
                "Electronica",
                new BigDecimal("18.00"),
                "Sevilla",
                "Sevilla",
                null,
                active
        );
    }
}
