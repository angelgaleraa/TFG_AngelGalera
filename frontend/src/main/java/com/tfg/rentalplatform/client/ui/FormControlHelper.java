package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

public final class FormControlHelper {

    private FormControlHelper() {
    }

    public static void configureDescriptionArea(TextArea area, int maxLength) {
        limitTextInput(area, maxLength);
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.setMinHeight(96);
        area.setMaxHeight(Region.USE_PREF_SIZE);
        area.getStyleClass().add("expanding-description-area");
        area.textProperty().addListener((obs, oldValue, newValue) -> resizeDescriptionArea(area));
        resizeDescriptionArea(area);
    }

    public static void limitTextInput(TextInputControl control, int maxLength) {
        control.setTextFormatter(new TextFormatter<String>(change -> {
            String next = change.getControlNewText();
            return next != null && next.length() <= maxLength ? change : null;
        }));
    }

    public static void attachFeedbackClearListener(Node target) {
        if (Boolean.TRUE.equals(target.getProperties().get("fieldFeedbackListenerAttached"))) {
            return;
        }
        target.getProperties().put("fieldFeedbackListenerAttached", true);
        if (target instanceof TextInputControl input) {
            input.textProperty().addListener((obs, oldValue, newValue) -> clearFieldFeedback(target));
        } else if (target instanceof ComboBox<?> combo) {
            combo.valueProperty().addListener((obs, oldValue, newValue) -> clearFieldFeedback(target));
        } else if (target instanceof ToggleButton toggle) {
            toggle.selectedProperty().addListener((obs, oldValue, newValue) -> clearFieldFeedback(target));
        }
    }

    public static void setFieldFeedback(Node target, String message) {
        if (target == null) {
            return;
        }
        target.getStyleClass().remove("field-invalid");
        target.getStyleClass().add("field-invalid");
        Object label = target.getProperties().get("fieldFeedbackLabel");
        if (label instanceof Label feedback) {
            feedback.setText(message == null ? "" : message);
            feedback.setVisible(message != null && !message.isBlank());
            feedback.setManaged(message != null && !message.isBlank());
        }
    }

    public static void clearFieldFeedback(Node target) {
        if (target == null) {
            return;
        }
        target.getStyleClass().remove("field-invalid");
        Object label = target.getProperties().get("fieldFeedbackLabel");
        if (label instanceof Label feedback) {
            feedback.setText("");
            feedback.setVisible(false);
            feedback.setManaged(false);
        }
    }

    public static void configurePriceField(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change -> {
            String next = change.getControlNewText();
            if (next == null || next.isBlank()) {
                return change;
            }
            return next.matches("\\d{0,6}([\\.,]\\d{0,2})?") ? change : null;
        }));
    }

    private static void resizeDescriptionArea(TextArea area) {
        String text = area.getText() == null ? "" : area.getText();
        int explicitLines = text.split("\\R", -1).length;
        int wrappedLines = Math.max(1, text.length() / 85 + 1);
        int rows = Math.min(12, Math.max(4, explicitLines + wrappedLines - 1));
        area.setPrefRowCount(rows);
        area.setPrefHeight(32 + rows * 22);
    }
}
