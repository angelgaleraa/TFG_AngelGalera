package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.mapper.ItemMapper;
import com.tfg.rentalplatform.client.model.ItemRow;
import com.tfg.rentalplatform.client.ui.CatalogViewFactory;
import com.tfg.rentalplatform.client.util.CatalogFilter;
import com.tfg.rentalplatform.client.util.LocationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.tfg.rentalplatform.client.util.CatalogOptions.displayCategory;
import static com.tfg.rentalplatform.client.util.LocationUtils.locationKey;

public final class CatalogController {

    private final ApiClient api;
    private final ObservableList<ItemRow> catalogItems;
    private final ObservableList<ItemRow> filterSourceItems;
    private final List<String> itemCategories;
    private final String filterAllLabel;
    private final String sortRecentLabel;
    private final Supplier<List<String>> provinceOptions;
    private final Map<String, List<String>> municipalitiesByProvince;
    private final BooleanSupplier loggedCheck;
    private final Runnable renderCards;
    private final Consumer<String> logger;
    private final ObservableList<String> titleSuggestions = FXCollections.observableArrayList();

    private TextField searchField;
    private ContextMenu suggestionsMenu;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> cityCombo;
    private ComboBox<String> municipalityCombo;
    private ComboBox<String> sortCombo;
    private boolean updatingFilters;
    private boolean loadingFilterSource;

    public CatalogController(
            ApiClient api,
            ObservableList<ItemRow> catalogItems,
            ObservableList<ItemRow> filterSourceItems,
            List<String> itemCategories,
            String filterAllLabel,
            String sortRecentLabel,
            Supplier<List<String>> provinceOptions,
            Map<String, List<String>> municipalitiesByProvince,
            BooleanSupplier loggedCheck,
            Runnable renderCards,
            Consumer<String> logger
    ) {
        this.api = api;
        this.catalogItems = catalogItems;
        this.filterSourceItems = filterSourceItems;
        this.itemCategories = itemCategories;
        this.filterAllLabel = filterAllLabel;
        this.sortRecentLabel = sortRecentLabel;
        this.provinceOptions = provinceOptions;
        this.municipalitiesByProvince = municipalitiesByProvince;
        this.loggedCheck = loggedCheck;
        this.renderCards = renderCards;
        this.logger = logger;
    }

    public void bind(CatalogViewFactory.CatalogViewParts view) {
        searchField = view.searchField();
        suggestionsMenu = view.suggestionsMenu();
        categoryCombo = view.categoryCombo();
        cityCombo = view.cityCombo();
        municipalityCombo = view.municipalityCombo();
        sortCombo = view.sortCombo();
    }

    public void onCategoryChanged() {
        if (!updatingFilters) {
            refreshFromControls();
        }
    }

    public void onCityChanged() {
        if (!updatingFilters) {
            updateMunicipalityOptions(null);
            refreshFromControls();
        }
    }

    public void onMunicipalityChanged() {
        if (!updatingFilters) {
            refreshFromControls();
        }
    }

    public void onSortChanged() {
        if (!updatingFilters) {
            refreshFromControls();
        }
    }

    public void refresh() {
        refresh(null, null, null, null);
    }

    public void refresh(String query, String category, String city, String municipality) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("query", query);
            params.put("category", category);
            params.put("city", city);
            params.put("municipality", municipality);
            String sort = selectedSort();
            if (sort != null) {
                params.put("sort", sort);
            }
            params.put("page", "0");
            params.put("size", "60");
            Map<String, Object> response = api.getMap(api.withQuery("/api/items", params));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            List<ItemRow> rows = content.stream().map(ItemMapper::toItemRow).toList();
            if (rows.isEmpty() && CatalogFilter.hasFilters(query, category, city, municipality) && !filterSourceItems.isEmpty()) {
                rows = filterSource(query, category, city, municipality);
            }
            catalogItems.setAll(rows);
            if (filterSourceItems.isEmpty() && !loadingFilterSource) {
                filterSourceItems.setAll(catalogItems);
            }
            refreshFilterOptions();
            renderCards.run();
            logger.accept("Catalogo cargado: " + catalogItems.size() + " objetos");
        } catch (Exception ex) {
            logger.accept("Error en catalogo: " + ex.getMessage());
        }
    }

    public void refreshFromControls() {
        if (updatingFilters) {
            return;
        }
        refresh(
                searchField == null ? null : searchField.getText(),
                selectedComboValue(categoryCombo),
                selectedComboValue(cityCombo),
                selectedComboValue(municipalityCombo)
        );
    }

    public void clearFilters() {
        updatingFilters = true;
        if (categoryCombo != null) {
            categoryCombo.setValue(filterAllLabel);
        }
        if (cityCombo != null) {
            cityCombo.setValue(filterAllLabel);
        }
        if (municipalityCombo != null) {
            municipalityCombo.setValue(filterAllLabel);
            municipalityCombo.setDisable(true);
        }
        if (sortCombo != null) {
            sortCombo.setValue(sortRecentLabel);
        }
        updatingFilters = false;
        refresh();
    }

    public void clearSearchText() {
        if (searchField != null) {
            searchField.clear();
            if (suggestionsMenu != null) {
                suggestionsMenu.hide();
            }
        }
        refreshFromControls();
    }

    @SuppressWarnings("unchecked")
    public void loadFilterSource() {
        if (!loggedCheck.getAsBoolean() || loadingFilterSource) {
            return;
        }
        loadingFilterSource = true;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "100");
            Map<String, Object> response = api.getMap(api.withQuery("/api/items", params));
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            filterSourceItems.setAll(content.stream().map(ItemMapper::toItemRow).toList());
            refreshFilterOptions();
        } catch (Exception ex) {
            logger.accept("Error al cargar filtros: " + ex.getMessage());
        } finally {
            loadingFilterSource = false;
        }
    }

    public void showSuggestions(String text) {
        if (suggestionsMenu == null || searchField == null) {
            return;
        }
        if (text == null || text.isBlank()) {
            suggestionsMenu.hide();
            return;
        }
        List<String> matches = titleSuggestions.stream()
                .filter(title -> title.toLowerCase().contains(text.toLowerCase()))
                .limit(6)
                .toList();
        if (matches.isEmpty()) {
            suggestionsMenu.hide();
            return;
        }
        suggestionsMenu.getItems().setAll(matches.stream().map(title -> {
            MenuItem item = new MenuItem(title);
            item.setOnAction(e -> {
                searchField.setText(title);
                searchField.positionCaret(title.length());
                suggestionsMenu.hide();
                refreshFromControls();
            });
            return item;
        }).toList());
        if (!suggestionsMenu.isShowing()) {
            suggestionsMenu.show(searchField, Side.BOTTOM, 0, 4);
        }
    }

    private List<ItemRow> filterSource(String query, String category, String city, String municipality) {
        return CatalogFilter.filter(filterSourceItems, query, category, city, municipality, selectedSort());
    }

    private String selectedComboValue(ComboBox<String> combo) {
        if (combo == null || combo.getValue() == null || filterAllLabel.equals(combo.getValue())) {
            return null;
        }
        return combo.getValue();
    }

    private String selectedSort() {
        if (sortCombo == null || sortCombo.getValue() == null) {
            return null;
        }
        return switch (sortCombo.getValue()) {
            case "Precio menor a mayor" -> "priceAsc";
            case "Precio mayor a menor" -> "priceDesc";
            default -> null;
        };
    }

    private void refreshFilterOptions() {
        ObservableList<ItemRow> source = filterSourceItems.isEmpty() ? catalogItems : filterSourceItems;
        Set<String> provinces = new LinkedHashSet<>(provinceOptions.get());
        source.stream().map(ItemRow::city).filter(CatalogFilter::hasText).forEach(provinces::add);

        Set<String> categories = new LinkedHashSet<>(itemCategories);
        source.stream().map(item -> displayCategory(item.category()))
                .filter(CatalogFilter::hasText)
                .forEach(categories::add);

        updateTitleSuggestions(source.stream().map(ItemRow::title).distinct().sorted().toList());
        updateComboOptions(categoryCombo, categories.stream().toList(), filterAllLabel);
        updateComboOptions(cityCombo, provinces.stream().toList(), filterAllLabel);
        updateMunicipalityOptions(municipalityCombo == null ? null : municipalityCombo.getValue());
    }

    private void updateMunicipalityOptions(String selectedMunicipality) {
        if (municipalityCombo == null) {
            return;
        }
        boolean wasUpdating = updatingFilters;
        updatingFilters = true;
        try {
            String selectedProvince = selectedComboValue(cityCombo);
            if (selectedProvince == null) {
                updateComboOptions(municipalityCombo, List.of(), filterAllLabel);
                municipalityCombo.setDisable(true);
                return;
            }

            ObservableList<ItemRow> source = filterSourceItems.isEmpty() ? catalogItems : filterSourceItems;
            String resolvedProvince = resolveProvinceName(selectedProvince);
            String selectedProvinceKey = locationKey(selectedProvince);
            Set<String> municipalities = new LinkedHashSet<>(
                    municipalitiesByProvince.getOrDefault(resolvedProvince, List.of())
            );
            source.stream()
                    .filter(item -> selectedProvinceKey.equals(locationKey(item.city())))
                    .map(ItemRow::municipality)
                    .map(LocationUtils::displayLocationText)
                    .filter(CatalogFilter::hasText)
                    .forEach(municipalities::add);

            updateComboOptions(municipalityCombo, municipalities.stream().toList(), filterAllLabel);
            municipalityCombo.setDisable(municipalities.isEmpty());
            if (selectedMunicipality != null && municipalityCombo.getItems().contains(selectedMunicipality)) {
                municipalityCombo.setValue(selectedMunicipality);
            }
        } finally {
            updatingFilters = wasUpdating;
        }
    }

    private String resolveProvinceName(String province) {
        return municipalitiesByProvince.keySet().stream()
                .filter(key -> locationKey(key).equals(locationKey(province)))
                .findFirst()
                .orElse(province);
    }

    private void updateTitleSuggestions(List<String> values) {
        String currentText = searchField == null ? "" : searchField.getText();
        titleSuggestions.setAll(values.stream().filter(CatalogFilter::hasText).toList());
        if (searchField != null && currentText != null) {
            searchField.setText(currentText);
            searchField.positionCaret(currentText.length());
        }
    }

    private void updateComboOptions(ComboBox<String> combo, List<String> values, String allLabel) {
        if (combo == null) {
            return;
        }
        boolean wasUpdating = updatingFilters;
        updatingFilters = true;
        try {
            String previous = combo.isEditable() ? combo.getEditor().getText() : combo.getValue();
            ObservableList<String> options = FXCollections.observableArrayList();
            if (allLabel != null) {
                options.add(allLabel);
            }
            options.addAll(values.stream().filter(CatalogFilter::hasText).toList());
            combo.setItems(options);
            if (allLabel != null) {
                combo.setValue(previous == null || previous.isBlank() || !options.contains(previous) ? allLabel : previous);
            } else if (previous != null && !previous.isBlank()) {
                combo.getEditor().setText(previous);
            }
        } finally {
            updatingFilters = wasUpdating;
        }
    }
}
