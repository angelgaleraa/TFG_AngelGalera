package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

public final class ResponsiveCardGrid {

    private static final double MIN_CARD_WIDTH = 330;

    private ResponsiveCardGrid() {
    }

    public static void syncWidth(FlowPane pane, double viewportWidth) {
        if (pane == null || viewportWidth <= 0) {
            return;
        }
        pane.setMinWidth(viewportWidth);
        pane.setPrefWidth(viewportWidth);
        pane.setPrefWrapLength(viewportWidth);
        applyCardWidths(pane, viewportWidth);
    }

    public static void applyCardWidths(FlowPane pane, double viewportWidth) {
        if (pane == null || viewportWidth <= 0 || pane.getChildren().isEmpty()) {
            return;
        }
        double gap = pane.getHgap();
        double usableWidth = Math.max(260, viewportWidth);
        int columns = Math.max(1, (int) Math.floor((usableWidth + gap) / (MIN_CARD_WIDTH + gap)));
        double cardWidth = Math.floor((usableWidth - gap * (columns - 1)) / columns);

        for (Node child : pane.getChildren()) {
            if (child instanceof Region region) {
                region.setMinWidth(cardWidth);
                region.setPrefWidth(cardWidth);
                region.setMaxWidth(cardWidth);
            }
        }
    }
}
