package com.tfg.rentalplatform.client.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class PaymentsViewFactory {

    public static final String PAYER_ENDPOINT = "/api/payments/me";
    public static final String OWNER_ENDPOINT = "/api/payments/owner";

    private PaymentsViewFactory() {
    }

    public static PaymentsViewParts create(
            ObservableList<Map<String, Object>> payments,
            String currentEndpoint,
            BooleanSupplier ownerView,
            Consumer<String> selectEndpoint
    ) {
        VBox summaryBox = new VBox(12);
        summaryBox.getStyleClass().add("payments-summary-grid");

        ListView<Map<String, Object>> listView = new ListView<>(payments);
        listView.getStyleClass().addAll("payment-list", "data-list");
        listView.setCellFactory(list -> paymentCell(ownerView));
        VBox.setVgrow(listView, Priority.ALWAYS);

        ToggleGroup paymentModeGroup = new ToggleGroup();
        ToggleButton payerButton = new ToggleButton("Gastos");
        ToggleButton ownerButton = new ToggleButton("Ingresos");
        payerButton.getStyleClass().add("mode-btn");
        ownerButton.getStyleClass().add("mode-btn");
        payerButton.setToggleGroup(paymentModeGroup);
        ownerButton.setToggleGroup(paymentModeGroup);
        payerButton.setSelected(!OWNER_ENDPOINT.equals(currentEndpoint));
        ownerButton.setSelected(OWNER_ENDPOINT.equals(currentEndpoint));
        payerButton.setOnAction(e -> selectEndpoint.accept(PAYER_ENDPOINT));
        ownerButton.setOnAction(e -> selectEndpoint.accept(OWNER_ENDPOINT));

        HBox toolbar = new HBox(payerButton, ownerButton);
        toolbar.getStyleClass().add("mode-selector");

        VBox root = new VBox(12, toolbar, summaryBox, listView);
        root.getStyleClass().add("page-panel");
        VBox.setVgrow(root, Priority.ALWAYS);

        return new PaymentsViewParts(root, summaryBox, listView);
    }

    private static ListCell<Map<String, Object>> paymentCell(BooleanSupplier ownerView) {
        return new ListCell<>() {
            {
                addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (!isEmpty()) {
                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(Map<String, Object> payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("payment-cell-filled");
                    return;
                }
                setText(null);
                setGraphic(ActivityRowFactory.paymentRow(payment, ownerView.getAsBoolean()));
                if (!getStyleClass().contains("payment-cell-filled")) {
                    getStyleClass().add("payment-cell-filled");
                }
            }
        };
    }

    public record PaymentsViewParts(Node root, VBox summaryBox, ListView<Map<String, Object>> listView) {
    }
}
