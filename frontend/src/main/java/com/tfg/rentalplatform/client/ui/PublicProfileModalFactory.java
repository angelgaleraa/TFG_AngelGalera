package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

import static com.tfg.rentalplatform.client.ui.ReviewUiFactory.createStarRating;
import static com.tfg.rentalplatform.client.ui.ReviewUiFactory.reviewsSectionHeader;
import static com.tfg.rentalplatform.client.ui.UiFactory.profileMetric;
import static com.tfg.rentalplatform.client.util.FormatUtils.toDouble;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;

public final class PublicProfileModalFactory {

    private PublicProfileModalFactory() {
    }

    @FunctionalInterface
    public interface ReportButtonFactory {
        Button create(Long userId, String userName);
    }

    @SuppressWarnings("unchecked")
    public static Node create(Map<String, Object> response, Long userId, ReportButtonFactory reportButtonFactory) {
        String name = nullableString(response.get("name"));
        String bio = nullableString(response.get("bio"));
        double average = toDouble(response.get("averageRating"));

        Label avatar = new Label(profileInitial(name));
        avatar.getStyleClass().add("profile-avatar");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("public-profile-name");

        HBox ratingLabel = createStarRating(average, true);
        ratingLabel.getStyleClass().add("public-profile-rating");

        Label bioLabel = new Label(bio.isBlank() ? "Este usuario todavia no ha escrito una biografia." : bio);
        bioLabel.setWrapText(true);
        bioLabel.getStyleClass().add("public-profile-bio");

        HBox nameRow = new HBox(10, nameLabel, reportButtonFactory.create(userId, name));
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        VBox identity = new VBox(6, nameRow, ratingLabel);
        HBox hero = new HBox(14, avatar, identity);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("public-profile-hero");

        HBox stats = new HBox(10,
                profileMetric("Opiniones", response.get("receivedReviews")),
                profileMetric("Objetos", response.get("publishedItems")),
                profileMetric("Alquileres", response.get("rentalsAsOwner"))
        );
        stats.getStyleClass().add("public-profile-metrics");

        VBox reviewsBox = new VBox(10);
        reviewsBox.getStyleClass().add("public-profile-reviews");
        List<Map<String, Object>> reviewData = (List<Map<String, Object>>) response.get("reviews");
        if (reviewData == null || reviewData.isEmpty()) {
            Label empty = new Label("Este perfil aun no tiene opiniones.");
            empty.getStyleClass().add("empty-state");
            reviewsBox.getChildren().add(empty);
        } else {
            reviewData.stream()
                    .map(ReviewUiFactory::profileReviewCard)
                    .forEach(reviewsBox.getChildren()::add);
        }

        VBox content = new VBox(16,
                hero,
                bioLabel,
                stats,
                reviewsSectionHeader(),
                reviewsBox
        );
        content.getStyleClass().add("form-stack");
        return content;
    }

    private static String profileInitial(String name) {
        return name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
