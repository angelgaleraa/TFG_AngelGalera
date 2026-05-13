package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

public final class ModalFactory {

    private ModalFactory() {
    }

    public static VBox modalCard(
            String title,
            String subtitle,
            Node content,
            Node footer,
            String cardStyle,
            double width,
            double maxContentHeight,
            Runnable close
    ) {
        Label modalTitle = new Label(title);
        modalTitle.getStyleClass().add("modal-title");
        Label modalSubtitle = new Label(subtitle);
        modalSubtitle.getStyleClass().add("modal-subtitle");

        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon());
        closeButton.getStyleClass().add("modal-close-btn");
        closeButton.setOnAction(e -> close.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, new VBox(4, modalTitle, modalSubtitle), spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("modal-header");

        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setMaxHeight(maxContentHeight);
        contentScroll.getStyleClass().add("surface-scroll");

        VBox card = new VBox(18, header, contentScroll);
        if (footer != null) {
            footer.getStyleClass().add("modal-footer");
            card.getChildren().add(footer);
        }
        for (String style : cardStyle.split(" ")) {
            if (!style.isBlank()) {
                card.getStyleClass().add(style);
            }
        }
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setOnMouseClicked(e -> e.consume());
        return card;
    }

    private static StackPane closeIcon() {
        StackPane icon = new StackPane();
        Line lineA = new Line(0, 0, 12, 12);
        Line lineB = new Line(12, 0, 0, 12);
        lineA.getStyleClass().add("modal-close-icon-line");
        lineB.getStyleClass().add("modal-close-icon-line");
        icon.getChildren().addAll(lineA, lineB);
        return icon;
    }
}
