package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.ui.UiFactory.*;

public final class AdminViewFactory {

    private AdminViewFactory() {
    }

    public static AdminViewParts create(
            Runnable renderReports,
            Consumer<String> showSuggestions,
            Runnable clearSearch
    ) {
        Label usersMetric = new Label("--");
        Label itemsMetric = new Label("--");
        Label reservationsMetric = new Label("--");
        Label reportsMetric = new Label("--");

        HBox metrics = new HBox(12,
                adminMetricCard("Usuarios", usersMetric),
                adminMetricCard("Objetos activos", itemsMetric),
                adminMetricCard("Reservas", reservationsMetric),
                adminMetricCard("Reportes abiertos", reportsMetric)
        );
        metrics.getStyleClass().add("admin-metrics");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox reportsHeader = new HBox(12, sectionTitle("Reportes recientes"), headerSpacer);
        reportsHeader.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        Button searchClearButton = new Button();
        searchField.setPromptText("Buscar por objeto, usuario o motivo");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.getStyleClass().add("search-field-with-clear");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            boolean hasText = newValue != null && !newValue.isBlank();
            searchClearButton.setVisible(hasText);
            renderReports.run();
            showSuggestions.accept(newValue);
        });
        searchField.setOnAction(e -> renderReports.run());

        StackPane clearIcon = clearIcon();
        searchClearButton.setGraphic(clearIcon);
        searchClearButton.getStyleClass().add("search-clear-btn");
        searchClearButton.setVisible(false);
        searchClearButton.setOnAction(e -> clearSearch.run());

        StackPane searchBox = new StackPane(searchField, searchClearButton);
        searchBox.getStyleClass().add("search-box");
        searchBox.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(searchClearButton, Pos.CENTER_RIGHT);

        ContextMenu suggestionsMenu = new ContextMenu();
        suggestionsMenu.getStyleClass().add("catalog-suggestions-menu");

        VBox reportsBox = new VBox(10);
        reportsBox.getStyleClass().add("admin-report-list");
        reportsBox.getChildren().add(emptyState("Cargando reportes..."));

        ScrollPane reportsScroll = new ScrollPane(reportsBox);
        reportsScroll.setFitToWidth(true);
        reportsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reportsScroll.getStyleClass().add("surface-scroll");
        VBox.setVgrow(reportsScroll, Priority.ALWAYS);

        VBox reportsCard = new VBox(14, reportsHeader, searchBox, reportsScroll);
        reportsCard.getStyleClass().add("admin-section");
        VBox.setVgrow(reportsCard, Priority.ALWAYS);

        VBox layout = new VBox(14, metrics, reportsCard);
        layout.getStyleClass().add("page-panel");
        VBox.setVgrow(reportsCard, Priority.ALWAYS);

        return new AdminViewParts(
                layout,
                usersMetric,
                itemsMetric,
                reservationsMetric,
                reportsMetric,
                reportsBox,
                searchField,
                searchClearButton,
                suggestionsMenu
        );
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

    public record AdminViewParts(
            Node root,
            Label usersMetric,
            Label itemsMetric,
            Label reservationsMetric,
            Label reportsMetric,
            VBox reportsBox,
            TextField searchField,
            Button searchClearButton,
            ContextMenu suggestionsMenu
    ) {
    }
}
