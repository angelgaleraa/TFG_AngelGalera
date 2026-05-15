package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.function.Consumer;

public final class CatalogViewFactory {

    private CatalogViewFactory() {
    }

    public static CatalogViewParts create(
            String filterAllLabel,
            String sortRecentLabel,
            Consumer<String> showSuggestions,
            Runnable search,
            Runnable clearSearch,
            Runnable clearFilters,
            Runnable categoryChanged,
            Runnable cityChanged,
            Runnable municipalityChanged,
            Runnable sortChanged,
            Consumer<Double> syncCardsWidth
    ) {
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.getStyleClass().add("search-field-with-clear");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> showSuggestions.accept(newValue));
        searchField.setOnAction(e -> search.run());

        Button searchClearButton = new Button();
        searchClearButton.setGraphic(clearIcon());
        searchClearButton.getStyleClass().add("search-clear-btn");
        searchClearButton.setVisible(false);
        searchClearButton.setOnAction(e -> clearSearch.run());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            boolean hasText = newValue != null && !newValue.isBlank();
            searchClearButton.setVisible(hasText);
        });

        StackPane searchBox = new StackPane(searchField, searchClearButton);
        searchBox.getStyleClass().add("search-box");
        searchBox.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(searchClearButton, Pos.CENTER_RIGHT);

        ContextMenu suggestionsMenu = new ContextMenu();
        suggestionsMenu.getStyleClass().add("catalog-suggestions-menu");

        ComboBox<String> categoryCombo = filterCombo(filterAllLabel);
        ComboBox<String> cityCombo = filterCombo(filterAllLabel);
        ComboBox<String> municipalityCombo = filterCombo(filterAllLabel);
        municipalityCombo.setDisable(true);

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().setAll(sortRecentLabel, "Precio menor a mayor", "Precio mayor a menor");
        sortCombo.setValue(sortRecentLabel);
        sortCombo.setMaxWidth(Double.MAX_VALUE);

        Button searchButton = new Button("Buscar");
        searchButton.getStyleClass().add("primary-btn");
        searchButton.setOnAction(e -> search.run());

        Button clearButton = new Button("Limpiar");
        clearButton.setOnAction(e -> clearFilters.run());

        categoryCombo.setOnAction(e -> categoryChanged.run());
        cityCombo.setOnAction(e -> cityChanged.run());
        municipalityCombo.setOnAction(e -> municipalityChanged.run());
        sortCombo.setOnAction(e -> sortChanged.run());

        HBox searchRow = new HBox(10, searchBox, searchButton);
        searchRow.getStyleClass().add("toolbar-row");
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        VBox categoryFilter = filterControl("Categoria", categoryCombo);
        VBox cityFilter = filterControl("Provincia", cityCombo);
        VBox municipalityFilter = filterControl("Municipio", municipalityCombo);
        VBox sortFilter = filterControl("Ordenar", sortCombo);

        HBox filters = new HBox(10, categoryFilter, cityFilter, municipalityFilter, sortFilter, clearButton);
        filters.getStyleClass().add("toolbar-row");
        filters.setAlignment(Pos.BOTTOM_LEFT);
        for (Node filter : List.of(categoryFilter, cityFilter, municipalityFilter, sortFilter)) {
            HBox.setHgrow(filter, Priority.ALWAYS);
        }

        FlowPane cardsPane = new FlowPane();
        cardsPane.setHgap(16);
        cardsPane.setVgap(16);
        cardsPane.getStyleClass().add("cards-pane");

        ScrollPane catalogScroll = new ScrollPane(cardsPane);
        catalogScroll.setFitToWidth(true);
        catalogScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        catalogScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        catalogScroll.getStyleClass().add("surface-scroll");
        catalogScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> syncCardsWidth.accept(newBounds.getWidth()));
        VBox.setVgrow(catalogScroll, Priority.ALWAYS);

        VBox root = new VBox(12, searchRow, filters, catalogScroll);
        root.getStyleClass().add("page-panel");
        VBox.setVgrow(root, Priority.ALWAYS);

        return new CatalogViewParts(
                root,
                searchField,
                searchClearButton,
                suggestionsMenu,
                categoryCombo,
                cityCombo,
                municipalityCombo,
                sortCombo,
                cardsPane
        );
    }

    private static ComboBox<String> filterCombo(String initialValue) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setValue(initialValue);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private static VBox filterControl(String labelText, ComboBox<String> combo) {
        Label label = new Label(labelText);
        label.getStyleClass().add("filter-label");
        VBox wrapper = new VBox(6, label, combo);
        wrapper.getStyleClass().add("filter-control");
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    private static StackPane clearIcon() {
        StackPane icon = new StackPane();
        icon.setMinSize(12, 12);
        icon.setMaxSize(12, 12);
        Line lineA = new Line(0, 0, 12, 12);
        Line lineB = new Line(12, 0, 0, 12);
        lineA.getStyleClass().add("search-clear-icon-line");
        lineB.getStyleClass().add("search-clear-icon-line");
        icon.getChildren().addAll(lineA, lineB);
        return icon;
    }

    public record CatalogViewParts(
            Node root,
            TextField searchField,
            Button searchClearButton,
            ContextMenu suggestionsMenu,
            ComboBox<String> categoryCombo,
            ComboBox<String> cityCombo,
            ComboBox<String> municipalityCombo,
            ComboBox<String> sortCombo,
            FlowPane cardsPane
    ) {
    }
}
