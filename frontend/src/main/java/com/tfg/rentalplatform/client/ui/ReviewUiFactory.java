package com.tfg.rentalplatform.client.ui;

import com.tfg.rentalplatform.client.util.FormatUtils;
import com.tfg.rentalplatform.client.util.TextUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Map;

public final class ReviewUiFactory {

    private ReviewUiFactory() {
    }

    public static HBox reviewsSectionHeader() {
        Label marker = new Label();
        marker.getStyleClass().add("reviews-section-marker");
        Label title = new Label("Opiniones");
        title.getStyleClass().add("reviews-section-title");
        Label subtitle = new Label("Valoraciones de alquileres completados");
        subtitle.getStyleClass().add("reviews-section-subtitle");
        VBox text = new VBox(2, title, subtitle);
        HBox header = new HBox(10, marker, text);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("reviews-section-header");
        return header;
    }

    public static VBox profileReviewCard(Map<String, Object> review) {
        HBox rating = createStarRating(review.get("rating"), false);
        rating.getStyleClass().add("profile-review-rating");

        Label author = new Label(TextUtils.nullableString(review.get("authorName")).isBlank()
                ? "Usuario"
                : TextUtils.nullableString(review.get("authorName")));
        author.getStyleClass().add("profile-review-author");

        Label comment = new Label(TextUtils.nullableString(review.get("comment")));
        comment.setWrapText(true);
        comment.getStyleClass().add("profile-review-comment");

        HBox header = new HBox(10, rating, author);
        header.setAlignment(Pos.CENTER_LEFT);

        String itemTitle = TextUtils.nullableString(review.get("itemTitle"));
        String dates = FormatUtils.formatReservationDates(review);
        Label reservation = new Label(itemTitle.isBlank()
                ? dates
                : dates.isBlank() ? "Alquiler de " + itemTitle : "Alquiler de " + itemTitle + " - " + dates);
        reservation.getStyleClass().add("profile-review-reservation");
        reservation.setVisible(!reservation.getText().isBlank());
        reservation.setManaged(!reservation.getText().isBlank());

        VBox card = new VBox(8, header, reservation, comment);
        card.getStyleClass().add("profile-review-card");
        return card;
    }

    public static HBox createStarRating(Object rating, boolean showNumber) {
        HBox box = new HBox(3);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("star-rating");
        updateStarRating(box, rating, showNumber);
        return box;
    }

    public static void updateStarRating(HBox box, Object rating, boolean showNumber) {
        double value = Math.max(0, Math.min(5, FormatUtils.toDouble(rating)));
        box.getChildren().clear();
        for (int index = 0; index < 5; index++) {
            double fill = Math.max(0, Math.min(1, value - index));
            box.getChildren().add(starNode(fill));
        }
        if (showNumber) {
            Label number = new Label(String.format("%.1f", value));
            number.getStyleClass().add("star-rating-number");
            box.getChildren().add(number);
        }
    }

    private static StackPane starNode(double fill) {
        Label empty = new Label("\u2605");
        empty.getStyleClass().add("star-empty");
        Label filled = new Label("\u2605");
        filled.getStyleClass().add("star-filled");
        filled.setClip(new Rectangle(14 * fill, 18));

        StackPane star = new StackPane(empty, filled);
        star.setMinSize(14, 18);
        star.setPrefSize(14, 18);
        star.setMaxSize(14, 18);
        return star;
    }
}
