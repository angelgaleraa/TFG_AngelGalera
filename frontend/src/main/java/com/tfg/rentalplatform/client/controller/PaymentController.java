package com.tfg.rentalplatform.client.controller;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.ui.PaymentsViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.ui.UiFactory.paymentMetric;
import static com.tfg.rentalplatform.client.util.FormatUtils.parseMoney;

public final class PaymentController {

    private final ApiClient api;
    private final ObservableList<Map<String, Object>> rows;
    private final BooleanSupplier loggedCheck;
    private final Consumer<String> logger;

    private VBox summaryBox;
    private String currentEndpoint = PaymentsViewFactory.PAYER_ENDPOINT;

    public PaymentController(
            ApiClient api,
            ObservableList<Map<String, Object>> rows,
            BooleanSupplier loggedCheck,
            Consumer<String> logger
    ) {
        this.api = api;
        this.rows = rows;
        this.loggedCheck = loggedCheck;
        this.logger = logger;
    }

    public Node createView() {
        PaymentsViewFactory.PaymentsViewParts view = PaymentsViewFactory.create(
                rows,
                currentEndpoint,
                this::isOwnerView,
                this::load
        );
        summaryBox = view.summaryBox();
        load(currentEndpoint);
        return view.root();
    }

    public void loadPayerPayments() {
        load(PaymentsViewFactory.PAYER_ENDPOINT);
    }

    public void clear() {
        rows.clear();
    }

    private void load(String endpoint) {
        if (!loggedCheck.getAsBoolean()) {
            return;
        }
        currentEndpoint = endpoint;
        try {
            List<Map<String, Object>> response = api.getList(endpoint);
            rows.setAll(response);
            renderSummary();
            logger.accept("Pagos cargados: " + rows.size());
        } catch (Exception ex) {
            logger.accept("Error en pagos: " + ex.getMessage());
        }
    }

    private void renderSummary() {
        if (summaryBox == null) {
            return;
        }
        double total = rows.stream().mapToDouble(row -> parseMoney(row.get("amount"))).sum();
        String totalLabel = isOwnerView() ? "Total de ingresos" : "Total de gastos";
        HBox cards = new HBox(12, paymentMetric(totalLabel, total));
        cards.getStyleClass().add("payments-summary-row");
        summaryBox.getChildren().setAll(cards);
    }

    private boolean isOwnerView() {
        return PaymentsViewFactory.OWNER_ENDPOINT.equals(currentEndpoint);
    }
}
