package com.tfg.rentalplatform.client.ui;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ItemFormHelper {

    private ItemFormHelper() {
    }

    public static boolean validate(
            ItemFormFields fields,
            int imageMaxLength,
            Consumer<Node> clearFeedback,
            BiConsumer<Node, String> setFeedback,
            Runnable invalidLogger
    ) {
        fields.clearFeedback(clearFeedback);
        boolean valid = true;
        if (isBlank(fields.titleField().getText())) {
            setFeedback.accept(fields.titleField(), "Escribe un titulo para el anuncio.");
            valid = false;
        }
        if (isBlank(fields.descriptionArea().getText())) {
            setFeedback.accept(fields.descriptionArea(), "Describe el objeto y su estado.");
            valid = false;
        }
        if (isBlank(fields.categoryCombo().getValue())) {
            setFeedback.accept(fields.categoryCombo(), "Selecciona una categoria.");
            valid = false;
        }
        if (isBlank(fields.provinceCombo().getValue())) {
            setFeedback.accept(fields.provinceCombo(), "Selecciona una provincia.");
            valid = false;
        }
        if (isBlank(fields.municipalityCombo().getValue())) {
            setFeedback.accept(fields.municipalityCombo(), "Selecciona un municipio.");
            valid = false;
        }
        if (isBlank(fields.priceField().getText())) {
            setFeedback.accept(fields.priceField(), "Indica el precio por dia.");
            valid = false;
        } else {
            valid = validatePrice(fields.priceField(), setFeedback) && valid;
        }
        if (fields.imageField().getText() != null && fields.imageField().getText().length() > imageMaxLength) {
            setFeedback.accept(fields.imageField(), "La ruta o URL de la imagen es demasiado larga.");
            valid = false;
        }
        if (!valid) {
            invalidLogger.run();
        }
        return valid;
    }

    public static Map<String, Object> payload(ItemFormFields fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", fields.titleField().getText().trim());
        payload.put("description", fields.descriptionArea().getText().trim());
        payload.put("category", fields.categoryCombo().getValue());
        payload.put("pricePerDay", Double.parseDouble(fields.priceField().getText().replace(",", ".")));
        payload.put("city", fields.provinceCombo().getValue());
        payload.put("municipality", fields.municipalityCombo().getValue());
        payload.put("imageUrl", fields.imageField().getText().trim());
        return payload;
    }

    public static Map<String, Object> payload(ItemFormFields fields, boolean active) {
        Map<String, Object> payload = payload(fields);
        payload.put("active", active);
        return payload;
    }

    private static boolean validatePrice(TextField priceField, BiConsumer<Node, String> setFeedback) {
        try {
            double price = Double.parseDouble(priceField.getText().replace(",", "."));
            if (price <= 0) {
                setFeedback.accept(priceField, "El precio debe ser mayor que cero.");
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            setFeedback.accept(priceField, "Introduce un precio valido, por ejemplo 12,50.");
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ItemFormFields(
            TextField titleField,
            TextArea descriptionArea,
            ComboBox<String> categoryCombo,
            TextField priceField,
            ComboBox<String> provinceCombo,
            ComboBox<String> municipalityCombo,
            TextField imageField
    ) {
        void clearFeedback(Consumer<Node> clearFeedback) {
            clearFeedback.accept(titleField);
            clearFeedback.accept(descriptionArea);
            clearFeedback.accept(categoryCombo);
            clearFeedback.accept(priceField);
            clearFeedback.accept(provinceCombo);
            clearFeedback.accept(municipalityCombo);
            clearFeedback.accept(imageField);
        }
    }
}
