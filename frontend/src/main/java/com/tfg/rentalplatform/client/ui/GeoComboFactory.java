package com.tfg.rentalplatform.client.ui;

import com.tfg.rentalplatform.client.util.LocationUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import java.util.*;

import static com.tfg.rentalplatform.client.util.LocationUtils.displayLocationText;
import static com.tfg.rentalplatform.client.util.LocationUtils.locationKey;

public final class GeoComboFactory {

    private GeoComboFactory() {
    }

    public static ComboBox<String> categoryCombo(List<String> categories, String selectedCategory) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(categories));
        combo.setEditable(false);
        combo.setMaxWidth(Double.MAX_VALUE);
        if (selectedCategory != null && !selectedCategory.isBlank()) {
            if (!combo.getItems().contains(selectedCategory)) {
                combo.getItems().add(selectedCategory);
            }
            combo.setValue(selectedCategory);
        } else {
            combo.setPromptText("Selecciona categoria");
        }
        return combo;
    }

    public static ComboBox<String> provinceCombo(Map<String, List<String>> municipalitiesByProvince, List<String> fallbackProvinces, String selectedProvince) {
        Set<String> provinces = new LinkedHashSet<>(provinceOptions(municipalitiesByProvince, fallbackProvinces));
        String resolvedSelectedProvince = resolveProvinceName(municipalitiesByProvince, selectedProvince);

        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(provinces));
        combo.setEditable(false);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setVisibleRowCount(12);
        if (resolvedSelectedProvince != null && !resolvedSelectedProvince.isBlank()) {
            if (!combo.getItems().contains(resolvedSelectedProvince)) {
                combo.getItems().add(resolvedSelectedProvince);
            }
            combo.setValue(resolvedSelectedProvince);
        } else {
            combo.setPromptText("Selecciona provincia");
        }
        return combo;
    }

    public static ComboBox<String> municipalityCombo(Map<String, List<String>> municipalitiesByProvince, String province, String selectedMunicipality) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setEditable(false);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setVisibleRowCount(12);
        combo.setPromptText("Selecciona municipio");
        updateMunicipalityOptions(combo, municipalitiesByProvince, province, selectedMunicipality);
        return combo;
    }

    public static void updateMunicipalityOptions(ComboBox<String> combo, Map<String, List<String>> municipalitiesByProvince, String province, String selectedMunicipality) {
        if (combo == null) {
            return;
        }
        String resolvedProvince = resolveProvinceName(municipalitiesByProvince, province);
        List<String> municipalities = municipalitiesByProvince.getOrDefault(resolvedProvince, List.of());
        String displaySelectedMunicipality = displayLocationText(selectedMunicipality);
        if (displaySelectedMunicipality != null && !displaySelectedMunicipality.isBlank() && !municipalities.contains(displaySelectedMunicipality)) {
            municipalities = new ArrayList<>(municipalities);
            municipalities.add(displaySelectedMunicipality);
            municipalities.sort(String::compareToIgnoreCase);
        }
        combo.setDisable(municipalities.isEmpty());
        combo.setItems(FXCollections.observableArrayList(municipalities));
        if (displaySelectedMunicipality != null && !displaySelectedMunicipality.isBlank() && municipalities.contains(displaySelectedMunicipality)) {
            combo.setValue(displaySelectedMunicipality);
        } else {
            combo.setValue(null);
        }
    }

    public static String resolveProvinceName(Map<String, List<String>> municipalitiesByProvince, String province) {
        if (province == null || province.isBlank()) {
            return null;
        }
        if (municipalitiesByProvince.containsKey(province)) {
            return province;
        }
        String normalizedProvince = locationKey(province);
        return municipalitiesByProvince.keySet().stream()
                .filter(candidate -> locationKey(candidate).equals(normalizedProvince))
                .findFirst()
                .orElse(province);
    }

    public static List<String> provinceOptions(Map<String, ?> provinceSource, List<String> fallbackProvinces) {
        if (provinceSource.isEmpty()) {
            return fallbackProvinces;
        }
        return provinceSource.keySet().stream()
                .map(LocationUtils::displayLocationText)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }
}
