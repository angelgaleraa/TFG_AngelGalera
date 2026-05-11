package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class UiFactory {

    private UiFactory() {
    }

    public static VBox adminMetricCard(String labelText, Label valueLabel) {
        valueLabel.getStyleClass().add("admin-metric-value");
        Label label = new Label(labelText);
        label.getStyleClass().add("admin-metric-label");
        VBox card = new VBox(6, valueLabel, label);
        card.getStyleClass().add("admin-metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    public static Label mutedText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        label.setWrapText(true);
        return label;
    }

    public static Label badge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("badge");
        return badge;
    }

    public static javafx.scene.control.Button actionButton(String text, String styleClass, Runnable action) {
        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        if (styleClass != null && !styleClass.isBlank()) {
            button.getStyleClass().add(styleClass);
        }
        button.setOnAction(e -> action.run());
        return button;
    }

    public static Label emptyState(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("empty-state");
        label.setWrapText(true);
        return label;
    }

    public static Label reservationMetaLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("reservation-row-meta");
        label.setWrapText(true);
        return label;
    }

    public static VBox paymentMetric(String label, double value) {
        Label title = new Label(label);
        title.getStyleClass().add("payment-metric-label");
        Label amount = new Label(String.format("%.2f EUR", value));
        amount.getStyleClass().add("payment-metric-value");
        VBox box = new VBox(6, title, amount);
        box.getStyleClass().add("payment-metric-card");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    public static VBox profileMetric(String labelText, Object value) {
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("profile-metric-value");
        Label label = new Label(labelText);
        label.getStyleClass().add("profile-metric-label");
        VBox metric = new VBox(3, valueLabel, label);
        metric.getStyleClass().add("profile-metric");
        HBox.setHgrow(metric, Priority.ALWAYS);
        return metric;
    }

    public static VBox fieldGroup(String labelText, Node control, Consumer<Node> feedbackClearListener) {
        return fieldGroup(labelText, control, control, feedbackClearListener);
    }

    public static VBox fieldGroup(String labelText, Node control, Node feedbackTarget, Consumer<Node> feedbackClearListener) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        Label feedback = new Label();
        feedback.getStyleClass().add("field-feedback");
        feedback.setVisible(false);
        feedback.setManaged(false);
        if (feedbackTarget != null) {
            feedbackTarget.getProperties().put("fieldFeedbackLabel", feedback);
            feedbackClearListener.accept(feedbackTarget);
        }
        VBox group = new VBox(6, label, control, feedback);
        group.getStyleClass().add("field-group");
        return group;
    }

    public static VBox infoTile(String labelText, Node value) {
        Label label = new Label(labelText);
        label.getStyleClass().add("info-tile-label");
        VBox tile = new VBox(5, label, value);
        tile.getStyleClass().add("info-tile");
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    public static VBox clickableInfoTile(String labelText, Node value, Runnable action) {
        VBox tile = infoTile(labelText, value);
        tile.getStyleClass().add("info-tile-clickable");
        tile.setOnMouseClicked(e -> action.run());
        return tile;
    }

    public static Label sectionTitle(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static VBox wrapCard(VBox content) {
        content.getStyleClass().add("card");
        return content;
    }
}
