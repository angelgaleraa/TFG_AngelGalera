package com.tfg.rentalplatform.client.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.tfg.rentalplatform.client.ui.UiFactory.sectionTitle;

public final class ReservationsViewFactory {

    private ReservationsViewFactory() {
    }

    public static ReservationsViewParts create(
            ObservableList<String> reservations,
            String currentReservationType,
            Function<String, Node> reservationGraphic,
            Consumer<String> selectReservation,
            Runnable loadMyReservations,
            Runnable loadOwnerReservations
    ) {
        ListView<String> listView = new ListView<>(reservations);
        listView.getStyleClass().addAll("data-list", "reservation-list");
        listView.setCellFactory(list -> reservationCell(reservationGraphic));
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> selectReservation.accept(newValue));
        VBox.setVgrow(listView, Priority.ALWAYS);

        ToggleGroup reservationModeGroup = new ToggleGroup();
        ToggleButton myButton = new ToggleButton("Mis reservas");
        ToggleButton ownerButton = new ToggleButton("Reservas de mis objetos");
        myButton.getStyleClass().add("mode-btn");
        ownerButton.getStyleClass().add("mode-btn");
        myButton.setToggleGroup(reservationModeGroup);
        ownerButton.setToggleGroup(reservationModeGroup);
        myButton.setSelected(!"Propietario".equals(currentReservationType));
        ownerButton.setSelected("Propietario".equals(currentReservationType));
        myButton.setOnAction(e -> loadMyReservations.run());
        ownerButton.setOnAction(e -> loadOwnerReservations.run());
        HBox toolbar = new HBox(myButton, ownerButton);
        toolbar.getStyleClass().add("mode-selector");

        Label selectedLabel = new Label("Sin reserva seleccionada");
        selectedLabel.getStyleClass().add("selection-info");

        VBox listPane = new VBox(12, toolbar, listView);
        listPane.getStyleClass().add("reservation-list-pane");
        HBox.setHgrow(listPane, Priority.ALWAYS);

        VBox detailBox = new VBox(14, sectionTitle("Detalle"), selectedLabel, new Separator());
        detailBox.getStyleClass().add("reservation-detail-pane");
        detailBox.setMinWidth(360);
        detailBox.setPrefWidth(390);
        detailBox.setMaxWidth(420);

        HBox root = new HBox(14, listPane, detailBox);
        root.getStyleClass().add("reservations-layout");
        VBox.setVgrow(root, Priority.ALWAYS);

        return new ReservationsViewParts(root, listView, selectedLabel, detailBox);
    }

    private static ListCell<String> reservationCell(Function<String, Node> reservationGraphic) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(reservationGraphic.apply(item));
            }
        };
    }

    public record ReservationsViewParts(
            Node root,
            ListView<String> listView,
            Label selectedLabel,
            VBox detailBox
    ) {
    }
}
