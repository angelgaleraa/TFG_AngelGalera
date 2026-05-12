package com.tfg.rentalplatform.client.util;

import com.tfg.rentalplatform.client.ui.GeoComboFactory;
import javafx.scene.control.ComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoDataService {

    private final List<String> fallbackProvinces;
    private final Map<String, String> provinceIdsByName = new HashMap<>();
    private final Map<String, List<String>> municipalitiesByProvince = new HashMap<>();

    public GeoDataService(List<String> fallbackProvinces) {
        this.fallbackProvinces = fallbackProvinces;
    }

    public void load(Object owner) {
        update(GeoDataLoader.load(owner, fallbackProvinces));
    }

    public void loadFallback() {
        update(GeoDataLoader.fallback(fallbackProvinces));
    }

    public List<String> provinceOptions() {
        return GeoComboFactory.provinceOptions(provinceIdsByName, fallbackProvinces);
    }

    public ComboBox<String> provinceCombo(String selectedProvince) {
        return GeoComboFactory.provinceCombo(municipalitiesByProvince, fallbackProvinces, selectedProvince);
    }

    public ComboBox<String> municipalityCombo(String province, String selectedMunicipality) {
        return GeoComboFactory.municipalityCombo(municipalitiesByProvince, province, selectedMunicipality);
    }

    public void updateMunicipalityOptions(ComboBox<String> combo, String province, String selectedMunicipality) {
        GeoComboFactory.updateMunicipalityOptions(combo, municipalitiesByProvince, province, selectedMunicipality);
    }

    public String resolveProvinceName(String province) {
        return GeoComboFactory.resolveProvinceName(municipalitiesByProvince, province);
    }

    public Map<String, List<String>> municipalitiesByProvince() {
        return municipalitiesByProvince;
    }

    private void update(GeoDataLoader.GeoData geoData) {
        provinceIdsByName.clear();
        municipalitiesByProvince.clear();
        provinceIdsByName.putAll(geoData.provinceIdsByName());
        municipalitiesByProvince.putAll(geoData.municipalitiesByProvince());
    }
}
