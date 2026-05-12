package com.tfg.rentalplatform.client.ui;

import com.tfg.rentalplatform.client.model.ItemRow;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.ui.UiFactory.*;
import static com.tfg.rentalplatform.client.util.CatalogOptions.displayCategory;

public final class CatalogDetailModalFactory {

    private CatalogDetailModalFactory() {
    }

    public static CatalogDetailParts create(
            ItemRow item,
            boolean adminMode,
            boolean showReportButton,
            Map<String, Image> imageCache,
            BiFunction<String, Node, VBox> fieldGroup,
            BookingSummaryUpdater bookingSummaryUpdater,
            Runnable reportItem,
            Runnable openOwnerProfile,
            Consumer<ItemRow> openChat,
            Runnable createReservation
    ) {
        StackPane media = ItemCardFactory.buildMedia(item, imageCache, 420, 330);
        media.getStyleClass().add("catalog-detail-media");

        Label title = new Label(item.title());
        title.getStyleClass().add("catalog-detail-title");
        title.setWrapText(true);
        title.setMaxWidth(500);

        Button reportItemButton = new Button("Reportar");
        reportItemButton.getStyleClass().add("report-secondary-btn");
        reportItemButton.setMinWidth(92);
        reportItemButton.setPrefWidth(92);
        reportItemButton.setVisible(showReportButton);
        reportItemButton.setManaged(reportItemButton.isVisible());
        reportItemButton.setOnAction(e -> reportItem.run());
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        HBox titleRow = new HBox(12, title, titleSpacer, reportItemButton);
        titleRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        titleRow.getStyleClass().add("catalog-detail-title-row");

        Label description = new Label(item.description());
        description.setWrapText(true);
        description.getStyleClass().add("catalog-detail-description");

        Label categoryValue = metaLabel(displayCategory(item.category()));
        Label cityValue = metaLabel(item.city());
        Label municipalityValue = metaLabel(item.municipality());
        Label ownerValue = metaLabel(item.ownerName());
        HBox metaRow = new HBox(10,
                infoTile("Categoria", categoryValue),
                infoTile("Provincia", cityValue),
                infoTile("Municipio", municipalityValue),
                clickableInfoTile("Propietario", ownerValue, openOwnerProfile)
        );
        metaRow.getStyleClass().add("catalog-detail-meta-row");

        DatePicker startPicker = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(2));
        VBox bookingPanel = bookingPanel(item, startPicker, endPicker, fieldGroup, bookingSummaryUpdater, openChat, createReservation);

        VBox details = new VBox(16, media, titleRow, description, metaRow);
        details.getStyleClass().add("catalog-detail-info");

        ScrollPane detailsScroll = new ScrollPane(details);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsScroll.getStyleClass().addAll("surface-scroll", "catalog-detail-scroll");
        HBox.setHgrow(detailsScroll, Priority.ALWAYS);

        HBox content = adminMode ? new HBox(24, detailsScroll) : new HBox(24, detailsScroll, bookingPanel);
        content.setFillHeight(false);
        content.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        content.getStyleClass().add("catalog-detail-layout");
        return new CatalogDetailParts(content, startPicker, endPicker);
    }

    private static VBox bookingPanel(
            ItemRow item,
            DatePicker startPicker,
            DatePicker endPicker,
            BiFunction<String, Node, VBox> fieldGroup,
            BookingSummaryUpdater bookingSummaryUpdater,
            Consumer<ItemRow> openChat,
            Runnable createReservation
    ) {
        Label summaryCaption = new Label("Total estimado");
        summaryCaption.getStyleClass().add("booking-summary-caption");
        Label summaryAmount = new Label();
        summaryAmount.getStyleClass().add("booking-summary-amount");
        Label summaryBreakdown = new Label();
        summaryBreakdown.getStyleClass().add("booking-summary-breakdown");
        Label summaryNote = new Label();
        summaryNote.setWrapText(true);
        summaryNote.getStyleClass().add("booking-summary-note");

        VBox summary = new VBox(5, summaryCaption, summaryAmount, summaryBreakdown, summaryNote);
        summary.getStyleClass().add("booking-summary");

        Runnable updateSummary = () -> bookingSummaryUpdater.update(
                item,
                startPicker.getValue(),
                endPicker.getValue(),
                summaryAmount,
                summaryBreakdown,
                summaryNote
        );
        startPicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && endPicker.getValue() != null && !endPicker.getValue().isAfter(newValue)) {
                endPicker.setValue(newValue.plusDays(1));
            }
            updateSummary.run();
        });
        endPicker.valueProperty().addListener((obs, oldValue, newValue) -> updateSummary.run());
        updateSummary.run();

        HBox dateRow = new HBox(12, fieldGroup.apply("Inicio", startPicker), fieldGroup.apply("Fin", endPicker));
        dateRow.getStyleClass().add("booking-date-row");

        Button reserveButton = new Button("Confirmar reserva");
        reserveButton.getStyleClass().add("primary-btn");
        reserveButton.setMaxWidth(Double.MAX_VALUE);
        reserveButton.setOnAction(e -> createReservation.run());

        Button contactButton = new Button("Contactar propietario");
        contactButton.setMaxWidth(Double.MAX_VALUE);
        contactButton.setOnAction(e -> openChat.accept(item));

        VBox bookingPanel = new VBox(12, sectionTitle("Reserva"), dateRow, summary, contactButton, reserveButton);
        bookingPanel.getStyleClass().addAll("booking-form", "booking-panel");
        bookingPanel.setMaxHeight(Region.USE_PREF_SIZE);
        return bookingPanel;
    }

    private static Label metaLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("catalog-detail-meta");
        return label;
    }

    @FunctionalInterface
    public interface BookingSummaryUpdater {
        void update(ItemRow item, LocalDate start, LocalDate end, Label totalAmount, Label totalBreakdown, Label totalNote);
    }

    public record CatalogDetailParts(Node content, DatePicker startPicker, DatePicker endPicker) {
    }
}
