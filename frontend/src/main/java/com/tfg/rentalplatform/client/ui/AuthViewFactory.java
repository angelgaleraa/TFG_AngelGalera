package com.tfg.rentalplatform.client.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.tfg.rentalplatform.client.ui.UiFactory.wrapCard;

public final class AuthViewFactory {

    private AuthViewFactory() {
    }

    public static AuthViewParts create(Runnable submit, Runnable showLoginMode, Runnable showRegisterMode) {
        Label title = new Label("Iniciar sesion");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Entra a tu espacio de trabajo.");
        subtitle.getStyleClass().add("auth-subtitle");

        ToggleButton loginModeButton = new ToggleButton("Iniciar sesion");
        ToggleButton registerModeButton = new ToggleButton("Crear cuenta");
        loginModeButton.getStyleClass().add("mode-btn");
        registerModeButton.getStyleClass().add("mode-btn");

        ToggleGroup modeGroup = new ToggleGroup();
        loginModeButton.setToggleGroup(modeGroup);
        registerModeButton.setToggleGroup(modeGroup);
        loginModeButton.setSelected(true);

        HBox modeSelector = new HBox(8, loginModeButton, registerModeButton);
        modeSelector.getStyleClass().add("mode-selector");

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre completo");
        TextField emailField = new TextField("ana@test.com");
        emailField.setPromptText("Correo electronico");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("1234");
        passwordField.setPromptText("Contrasena");

        VBox nameRow = new VBox(4, new Label("Nombre"), nameField);
        nameRow.managedProperty().bind(nameRow.visibleProperty());

        VBox emailRow = new VBox(4, new Label("Email"), emailField);
        VBox passwordRow = new VBox(4, new Label("Contrasena"), passwordField);

        Button submitButton = new Button("Entrar");
        submitButton.getStyleClass().add("primary-btn");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> submit.run());

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("auth-status");
        loginModeButton.setOnAction(e -> showLoginMode.run());
        registerModeButton.setOnAction(e -> showRegisterMode.run());

        VBox body = new VBox(12, title, subtitle, nameRow, emailRow, passwordRow, submitButton, statusLabel);
        VBox form = new VBox(12, modeSelector, body);
        return new AuthViewParts(
                wrapCard(form),
                title,
                subtitle,
                loginModeButton,
                registerModeButton,
                nameField,
                emailField,
                passwordField,
                nameRow,
                submitButton,
                statusLabel,
                body
        );
    }

    public record AuthViewParts(
            VBox root,
            Label title,
            Label subtitle,
            ToggleButton loginModeButton,
            ToggleButton registerModeButton,
            TextField nameField,
            TextField emailField,
            PasswordField passwordField,
            VBox nameRow,
            Button submitButton,
            Label statusLabel,
            VBox body
    ) {
    }
}
