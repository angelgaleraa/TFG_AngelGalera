package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiFunction;

import static com.tfg.rentalplatform.client.ui.ReviewUiFactory.createStarRating;
import static com.tfg.rentalplatform.client.ui.ReviewUiFactory.reviewsSectionHeader;
import static com.tfg.rentalplatform.client.ui.UiFactory.sectionTitle;

public final class ProfileViewFactory {

    private ProfileViewFactory() {
    }

    public static ProfileViewParts create(
            String currentUserName,
            String currentUserEmail,
            String profileInitial,
            BiFunction<String, Node, VBox> fieldGroup,
            Runnable saveProfile,
            Runnable changePassword
    ) {
        TextField nameField = new TextField(currentUserName == null ? "" : currentUserName);
        Label emailLabel = new Label(currentUserEmail == null ? "" : currentUserEmail);
        emailLabel.getStyleClass().add("selection-info");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Telefono de contacto");
        TextArea bioArea = new TextArea();
        bioArea.setPromptText("Cuenta algo sobre ti, tu disponibilidad o como gestionas tus alquileres.");
        bioArea.setPrefRowCount(3);

        Button saveButton = new Button("Guardar perfil");
        saveButton.getStyleClass().add("primary-btn");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> saveProfile.run());

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Contrasena actual");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nueva contrasena");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Repite la nueva contrasena");

        Button passwordButton = new Button("Actualizar contrasena");
        passwordButton.getStyleClass().add("primary-btn");
        passwordButton.setMaxWidth(Double.MAX_VALUE);
        passwordButton.setOnAction(e -> changePassword.run());

        VBox personalCard = new VBox(14,
                sectionTitle("Datos personales"),
                fieldGroup.apply("Nombre publico", nameField),
                fieldGroup.apply("Email", emailLabel),
                fieldGroup.apply("Telefono", phoneField),
                fieldGroup.apply("Biografia", bioArea),
                saveButton
        );
        personalCard.getStyleClass().add("profile-panel");

        VBox securityCard = new VBox(14,
                sectionTitle("Seguridad"),
                fieldGroup.apply("Contrasena actual", currentPasswordField),
                fieldGroup.apply("Nueva contrasena", newPasswordField),
                fieldGroup.apply("Confirmar contrasena", confirmPasswordField),
                passwordButton
        );
        securityCard.getStyleClass().add("profile-panel");

        VBox profileColumn = new VBox(16, personalCard, securityCard);
        profileColumn.getStyleClass().add("profile-column");

        Label reputationAvatar = new Label(profileInitial);
        reputationAvatar.getStyleClass().add("profile-avatar");
        Label reputationName = new Label(currentUserName == null ? "Usuario" : currentUserName);
        reputationName.getStyleClass().add("public-profile-name");
        HBox reputationRating = createStarRating(0, true);
        reputationRating.getStyleClass().add("public-profile-rating");
        VBox reputationIdentity = new VBox(6, reputationName, reputationRating);
        HBox reputationHero = new HBox(14, reputationAvatar, reputationIdentity);
        reputationHero.setAlignment(Pos.CENTER_LEFT);
        reputationHero.getStyleClass().add("public-profile-hero");

        HBox reputationStatsBox = new HBox(10);
        reputationStatsBox.getStyleClass().add("public-profile-metrics");
        VBox reviewsBox = new VBox(10);
        reviewsBox.getStyleClass().add("profile-reviews-list");

        VBox reputation = new VBox(14,
                sectionTitle("Reputacion"),
                reputationHero,
                reputationStatsBox,
                reviewsSectionHeader(),
                reviewsBox
        );
        reputation.getStyleClass().add("profile-panel");
        reputation.setMinWidth(430);
        VBox.setVgrow(reputation, Priority.ALWAYS);

        HBox layout = new HBox(18, profileColumn, reputation);
        layout.getStyleClass().add("profile-layout");
        HBox.setHgrow(profileColumn, Priority.ALWAYS);
        HBox.setHgrow(reputation, Priority.ALWAYS);
        VBox.setVgrow(layout, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("surface-scroll");

        return new ProfileViewParts(
                scroll,
                nameField,
                emailLabel,
                phoneField,
                bioArea,
                currentPasswordField,
                newPasswordField,
                confirmPasswordField,
                reputationAvatar,
                reputationName,
                reputationRating,
                reputationStatsBox,
                reviewsBox
        );
    }

    public record ProfileViewParts(
            Node root,
            TextField nameField,
            Label emailLabel,
            TextField phoneField,
            TextArea bioArea,
            PasswordField currentPasswordField,
            PasswordField newPasswordField,
            PasswordField confirmPasswordField,
            Label reputationAvatar,
            Label reputationName,
            HBox reputationRating,
            HBox reputationStatsBox,
            VBox reviewsBox
    ) {
    }
}
