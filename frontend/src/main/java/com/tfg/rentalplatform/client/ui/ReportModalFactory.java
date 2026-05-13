package com.tfg.rentalplatform.client.ui;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.function.BiFunction;

import static com.tfg.rentalplatform.client.util.TextUtils.cleanDisplayText;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;

public final class ReportModalFactory {

    private ReportModalFactory() {
    }

    public static ReportModalParts create(
            String targetName,
            BiFunction<String, Node, VBox> fieldGroup,
            ReportSubmitHandler submitReport
    ) {
        ComboBox<String> reasonCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Contenido enganoso",
                "Producto prohibido o peligroso",
                "Comportamiento inapropiado",
                "Posible fraude",
                "Otro motivo"
        ));
        reasonCombo.getSelectionModel().selectFirst();
        reasonCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Anade detalles para que el administrador pueda revisarlo mejor.");
        detailsArea.setPrefRowCount(4);

        Label target = new Label(nullableString(targetName).isBlank() ? "Contenido seleccionado" : cleanDisplayText(targetName));
        target.getStyleClass().add("selection-info");

        Button sendButton = new Button("Enviar reporte");
        sendButton.getStyleClass().add("primary-btn");
        sendButton.setOnAction(e -> submitReport.submit(reasonCombo.getValue(), detailsArea.getText()));

        VBox form = new VBox(14,
                fieldGroup.apply("Reportar", target),
                fieldGroup.apply("Motivo", reasonCombo),
                fieldGroup.apply("Detalles", detailsArea),
                sendButton
        );
        form.getStyleClass().add("form-stack");
        return new ReportModalParts(form);
    }

    @FunctionalInterface
    public interface ReportSubmitHandler {
        void submit(String reason, String details);
    }

    public record ReportModalParts(Node content) {
    }
}
