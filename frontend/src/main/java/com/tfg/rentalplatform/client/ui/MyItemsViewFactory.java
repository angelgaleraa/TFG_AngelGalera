package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class MyItemsViewFactory {

    private MyItemsViewFactory() {
    }

    public static MyItemsViewParts create(Runnable publishItem, Consumer<Double> syncCardsWidth) {
        Button publishButton = new Button("Publicar objeto");
        publishButton.getStyleClass().add("primary-btn");
        publishButton.setOnAction(e -> publishItem.run());

        FlowPane cardsPane = new FlowPane();
        cardsPane.setHgap(16);
        cardsPane.setVgap(16);
        cardsPane.getStyleClass().add("cards-pane");

        ScrollPane itemsScroll = new ScrollPane(cardsPane);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.getStyleClass().add("surface-scroll");
        itemsScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> syncCardsWidth.accept(newBounds.getWidth()));
        VBox.setVgrow(itemsScroll, Priority.ALWAYS);

        VBox root = new VBox(12, new HBox(10, publishButton), itemsScroll);
        root.getStyleClass().add("page-panel");
        VBox.setVgrow(root, Priority.ALWAYS);
        return new MyItemsViewParts(root, cardsPane);
    }

    public record MyItemsViewParts(Node root, FlowPane cardsPane) {
    }
}
