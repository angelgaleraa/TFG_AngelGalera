package com.tfg.rentalplatform.client.ui;

import com.tfg.rentalplatform.client.model.ItemRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.Map;
import java.util.function.Consumer;

public final class ItemCardFactory {

    private ItemCardFactory() {
    }

    public static VBox buildItemCard(
            ItemRow item,
            boolean editable,
            Map<String, Image> imageCache,
            Consumer<ItemRow> editableAction,
            Consumer<ItemRow> catalogAction
    ) {
        StackPane media = buildMedia(item, imageCache);

        Label title = new Label(item.title());
        title.getStyleClass().add("item-card-title");

        Label activeBadge = badge(item.moderationBlocked() ? "Retirado por admin" : item.active() ? "Disponible" : "Pausado");
        activeBadge.getStyleClass().add(item.active() && !item.moderationBlocked() ? "badge-success" : "badge-muted");
        if (editable) {
            StackPane.setAlignment(activeBadge, Pos.TOP_LEFT);
            StackPane.setMargin(activeBadge, new Insets(10));
            media.getChildren().add(activeBadge);
        }

        Label city = new Label(item.city() + " - " + item.municipality());
        city.getStyleClass().add("meta-text");
        Label owner = new Label(item.ownerName());
        owner.getStyleClass().add("meta-text");
        Label price = new Label(item.pricePerDay() + " EUR/dia");
        price.getStyleClass().add("price-text");

        HBox metaRow = new HBox(10, owner, city);
        HBox footer = new HBox(10, price, new Region());
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);
        VBox card = new VBox(12, media, title, verticalSpacer, metaRow, footer);

        card.getStyleClass().add("item-card");
        card.setUserData(item.id());
        card.setOnMouseClicked(e -> {
            if (editable) {
                editableAction.accept(item);
            } else {
                catalogAction.accept(item);
            }
        });
        card.setPrefWidth(310);
        card.setMaxWidth(420);
        card.setMinHeight(374);
        card.setPrefHeight(374);
        return card;
    }

    public static StackPane buildMedia(ItemRow item, Map<String, Image> imageCache) {
        return buildMedia(item, imageCache, 282, 220);
    }

    public static StackPane buildMedia(ItemRow item, Map<String, Image> imageCache, double width, double height) {
        Label mediaLabel = new Label(item.title() == null || item.title().isBlank()
                ? "?"
                : item.title().substring(0, 1).toUpperCase());
        mediaLabel.getStyleClass().add("item-media-letter");
        mediaLabel.setVisible(false);

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("item-media-image");
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        StackPane media = new StackPane(imageView, mediaLabel);
        media.getStyleClass().add("item-media");
        media.setPrefSize(width, height);
        media.setMaxWidth(Double.MAX_VALUE);
        imageView.fitWidthProperty().bind(media.widthProperty());
        imageView.fitHeightProperty().bind(media.heightProperty());

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        media.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());
            updateImageCoverViewport(imageView);
        });

        String imageUrl = item.imageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            mediaLabel.setVisible(true);
            return media;
        }

        try {
            Image cachedImage = imageCache.get(imageUrl);
            if (cachedImage != null && cachedImage.isError()) {
                imageCache.remove(imageUrl);
            }
            Image image = imageCache.computeIfAbsent(imageUrl, url -> new Image(url, 0, 0, true, true, true));
            mediaLabel.setVisible(false);
            image.errorProperty().addListener((obs, oldValue, hasError) -> {
                if (Boolean.TRUE.equals(hasError)) {
                    imageCache.remove(imageUrl);
                    imageView.setImage(null);
                    mediaLabel.setVisible(true);
                } else {
                    mediaLabel.setVisible(false);
                }
            });
            image.progressProperty().addListener((obs, oldValue, progress) -> {
                if (progress.doubleValue() >= 1.0 && !image.isError()) {
                    updateImageCoverViewport(imageView);
                    mediaLabel.setVisible(false);
                }
            });
            imageView.setImage(image);
            if (image.isError()) {
                imageView.setImage(null);
                mediaLabel.setVisible(true);
            } else {
                updateImageCoverViewport(imageView);
                mediaLabel.setVisible(false);
            }
        } catch (Exception ex) {
            imageView.setImage(null);
            mediaLabel.setVisible(true);
        }

        return media;
    }

    private static void updateImageCoverViewport(ImageView imageView) {
        Image image = imageView.getImage();
        if (image == null || image.isError() || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return;
        }

        double targetWidth = Math.max(1, imageView.getFitWidth());
        double targetHeight = Math.max(1, imageView.getFitHeight());
        if (imageView.getBoundsInParent().getWidth() > 1) {
            targetWidth = imageView.getBoundsInParent().getWidth();
        }
        if (imageView.getBoundsInParent().getHeight() > 1) {
            targetHeight = imageView.getBoundsInParent().getHeight();
        }

        double imageRatio = image.getWidth() / image.getHeight();
        double targetRatio = targetWidth / targetHeight;
        double viewportWidth = image.getWidth();
        double viewportHeight = image.getHeight();

        if (imageRatio > targetRatio) {
            viewportWidth = image.getHeight() * targetRatio;
        } else {
            viewportHeight = image.getWidth() / targetRatio;
        }

        double x = (image.getWidth() - viewportWidth) / 2;
        double y = (image.getHeight() - viewportHeight) / 2;
        imageView.setViewport(new Rectangle2D(x, y, viewportWidth, viewportHeight));
    }

    private static Label badge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("badge");
        return badge;
    }
}
