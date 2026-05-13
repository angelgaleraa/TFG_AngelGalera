package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.ui.AdminReportRowFactory;
import com.tfg.rentalplatform.client.ui.AdminViewFactory;
import com.tfg.rentalplatform.client.util.AdminReportSearch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.ui.UiFactory.emptyState;

public final class AdminOverviewController {

    private final ApiClient api;
    private final BooleanSupplier adminCheck;
    private final Consumer<String> logger;
    private final Consumer<Long> openItemDetail;
    private final Consumer<Long> openUserProfile;
    private final BiConsumer<Long, String> openChat;
    private final ObservableList<Map<String, Object>> reportRows = FXCollections.observableArrayList();

    private Label usersMetric;
    private Label itemsMetric;
    private Label reservationsMetric;
    private Label reportsMetric;
    private VBox reportsBox;
    private TextField searchField;
    private ContextMenu suggestionsMenu;

    public AdminOverviewController(
            ApiClient api,
            BooleanSupplier adminCheck,
            Consumer<String> logger,
            Consumer<Long> openItemDetail,
            Consumer<Long> openUserProfile,
            BiConsumer<Long, String> openChat
    ) {
        this.api = api;
        this.adminCheck = adminCheck;
        this.logger = logger;
        this.openItemDetail = openItemDetail;
        this.openUserProfile = openUserProfile;
        this.openChat = openChat;
    }

    public Node createView() {
        AdminViewFactory.AdminViewParts view = AdminViewFactory.create(
                this::renderReports,
                this::showSuggestions,
                this::clearSearch
        );
        usersMetric = view.usersMetric();
        itemsMetric = view.itemsMetric();
        reservationsMetric = view.reservationsMetric();
        reportsMetric = view.reportsMetric();
        reportsBox = view.reportsBox();
        searchField = view.searchField();
        suggestionsMenu = view.suggestionsMenu();
        return view.root();
    }

    @SuppressWarnings("unchecked")
    public void loadOverview() {
        if (!adminCheck.getAsBoolean()) {
            return;
        }
        try {
            Map<String, Object> response = api.getMap("/api/admin/overview");
            setMetric(usersMetric, response.get("users"));
            setMetric(itemsMetric, response.get("activeItems"));
            setMetric(reservationsMetric, response.get("reservations"));
            setMetric(reportsMetric, response.get("openReports"));

            List<Map<String, Object>> reports = (List<Map<String, Object>>) response.get("recentReports");
            reportRows.setAll(reports == null ? List.of() : reports);
            renderReports();
            logger.accept("Resumen admin cargado");
        } catch (Exception ex) {
            logger.accept("Error en resumen admin: " + ex.getMessage());
        }
    }

    private void renderReports() {
        if (reportsBox == null) {
            return;
        }
        reportsBox.getChildren().clear();
        List<Map<String, Object>> reports = filteredReports();
        if (reportRows.isEmpty()) {
            reportsBox.getChildren().add(emptyState("No hay reportes recientes."));
            return;
        }
        if (reports.isEmpty()) {
            reportsBox.getChildren().add(emptyState("No hay reportes que coincidan."));
            return;
        }
        reports.stream()
                .map(this::buildReportRow)
                .forEach(reportsBox.getChildren()::add);
    }

    private List<Map<String, Object>> filteredReports() {
        String query = searchField == null ? "" : searchField.getText();
        return AdminReportSearch.filterReports(reportRows, query);
    }

    private void showSuggestions(String text) {
        if (suggestionsMenu == null || searchField == null) {
            return;
        }
        if (text == null || text.isBlank()) {
            suggestionsMenu.hide();
            return;
        }
        List<String> matches = AdminReportSearch.matchingSuggestions(reportRows, text, 6);
        if (matches.isEmpty()) {
            suggestionsMenu.hide();
            return;
        }
        suggestionsMenu.getItems().setAll(matches.stream().map(value -> {
            MenuItem item = new MenuItem(value);
            item.setOnAction(e -> {
                searchField.setText(value);
                searchField.positionCaret(value.length());
                suggestionsMenu.hide();
                renderReports();
            });
            return item;
        }).toList());
        if (!suggestionsMenu.isShowing()) {
            suggestionsMenu.show(searchField, Side.BOTTOM, 0, 4);
        }
    }

    private void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
        if (suggestionsMenu != null) {
            suggestionsMenu.hide();
        }
        renderReports();
    }

    private Node buildReportRow(Map<String, Object> report) {
        return AdminReportRowFactory.create(
                report,
                openItemDetail,
                openUserProfile,
                openChat,
                this::updateReportAction,
                this::setUserActive
        );
    }

    private void updateReportAction(Long reportId, String action, String successMessage) {
        if (reportId == null || !adminCheck.getAsBoolean()) {
            return;
        }
        try {
            api.put("/api/admin/reports/" + reportId + "/" + action, Map.of());
            logger.accept(successMessage);
            loadOverview();
        } catch (Exception ex) {
            logger.accept("Error al actualizar reporte: " + ex.getMessage());
        }
    }

    private void setUserActive(Long userId, boolean active, String successMessage) {
        if (userId == null || !adminCheck.getAsBoolean()) {
            return;
        }
        try {
            api.put("/api/admin/users/" + userId + "/active?active=" + active, Map.of());
            logger.accept(successMessage);
            loadOverview();
        } catch (Exception ex) {
            logger.accept("Error al actualizar usuario: " + ex.getMessage());
        }
    }

    private void setMetric(Label label, Object value) {
        if (label != null) {
            label.setText(String.valueOf(value));
        }
    }
}
