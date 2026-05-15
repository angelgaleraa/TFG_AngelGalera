package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.mapper.ItemMapper;
import com.tfg.rentalplatform.client.model.ItemRow;
import com.tfg.rentalplatform.client.ui.ItemFormHelper;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class MyItemsController {

    private final ApiClient api;
    private final ObservableList<ItemRow> myItems;
    private final BooleanSupplier loggedCheck;
    private final BooleanSupplier myItemsViewCheck;
    private final Runnable renderCards;
    private final Runnable refreshCatalog;
    private final Runnable hideModal;
    private final Runnable showMyItemsView;
    private final Consumer<String> logger;

    private ItemRow selectedItem;

    public MyItemsController(ApiClient api,
                             ObservableList<ItemRow> myItems,
                             BooleanSupplier loggedCheck,
                             BooleanSupplier myItemsViewCheck,
                             Runnable renderCards,
                             Runnable refreshCatalog,
                             Runnable hideModal,
                             Runnable showMyItemsView,
                             Consumer<String> logger) {
        this.api = api;
        this.myItems = myItems;
        this.loggedCheck = loggedCheck;
        this.myItemsViewCheck = myItemsViewCheck;
        this.renderCards = renderCards;
        this.refreshCatalog = refreshCatalog;
        this.hideModal = hideModal;
        this.showMyItemsView = showMyItemsView;
        this.logger = logger;
    }

    public ItemRow selectedItem() {
        return selectedItem;
    }

    public void selectItem(ItemRow item) {
        selectedItem = item;
    }

    public void clearSelection() {
        selectedItem = null;
    }

    public void load() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            List<Map<String, Object>> response = api.getList("/api/items/me");
            myItems.setAll(response.stream().map(ItemMapper::toItemRow).toList());
            renderCards.run();
            logger.accept("Mis objetos cargados: " + myItems.size());
        } catch (Exception ex) {
            logger.accept("Error al cargar mis objetos: " + ex.getMessage());
        }
    }

    public void create(ItemFormHelper.ItemFormFields fields) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        try {
            Map<String, Object> response = api.post("/api/items", ItemFormHelper.payload(fields));
            ItemRow createdItem = ItemMapper.toItemRow(response);
            upsert(createdItem);
            selectedItem = createdItem;
            logger.accept("Objeto publicado correctamente.");
            hideModal.run();
            refreshCatalog.run();
            load();
            if (myItemsViewCheck.getAsBoolean()) {
                renderCards.run();
            } else {
                showMyItemsView.run();
            }
        } catch (Exception ex) {
            logger.accept("Error al crear objeto: " + ex.getMessage());
        }
    }

    public void update(ItemFormHelper.ItemFormFields fields, boolean active) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (selectedItem == null) {
            logger.accept("Selecciona primero un objeto de tu inventario.");
            return;
        }
        if (selectedItem.moderationBlocked() && active) {
            logger.accept("Este objeto ha sido retirado por administracion y no puede volver al catalogo.");
            return;
        }
        try {
            long itemId = selectedItem.id();
            api.put("/api/items/" + itemId, ItemFormHelper.payload(fields, active));
            logger.accept("Cambios guardados correctamente.");
            load();
            refreshCatalog.run();
            hideModal.run();
        } catch (Exception ex) {
            logger.accept("Error al actualizar objeto: " + ex.getMessage());
        }
    }

    public void deleteSelected() {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        if (selectedItem == null) {
            logger.accept("Selecciona primero un objeto de tu inventario.");
            return;
        }
        try {
            long itemId = selectedItem.id();
            api.delete("/api/items/" + itemId);
            logger.accept("Objeto eliminado de tu lista.");
            selectedItem = null;
            hideModal.run();
            remove(itemId);
            load();
            refreshCatalog.run();
        } catch (Exception ex) {
            logger.accept("Error al eliminar objeto: " + ex.getMessage());
        }
    }

    private void upsert(ItemRow item) {
        if (item == null) {
            return;
        }
        for (int i = 0; i < myItems.size(); i++) {
            if (myItems.get(i).id() == item.id()) {
                myItems.set(i, item);
                renderCards.run();
                return;
            }
        }
        myItems.add(0, item);
        renderCards.run();
    }

    private void remove(long itemId) {
        myItems.removeIf(item -> item.id() == itemId);
        renderCards.run();
    }
}
