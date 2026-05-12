package com.tfg.rentalplatform.client.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GeoDataLoader {

    private GeoDataLoader() {
    }

    public static GeoData load(Object owner, List<String> fallbackProvinces) {
        try (InputStream provinceStream = owner.getClass().getResourceAsStream("/geo/provincias.json");
             InputStream municipalityStream = owner.getClass().getResourceAsStream("/geo/municipios.json")) {
            if (provinceStream == null || municipalityStream == null) {
                return fallback(fallbackProvinces);
            }
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> provinces = mapper.readValue(provinceStream, new TypeReference<>() {});
            List<Map<String, String>> municipalities = mapper.readValue(municipalityStream, new TypeReference<>() {});
            return fromRows(provinces, municipalities);
        } catch (Exception ex) {
            return fallback(fallbackProvinces);
        }
    }

    public static GeoData fallback(List<String> fallbackProvinces) {
        Map<String, String> provinceIdsByName = new HashMap<>();
        Map<String, List<String>> municipalitiesByProvince = new HashMap<>();
        for (String province : fallbackProvinces) {
            provinceIdsByName.put(province, province);
            municipalitiesByProvince.put(province, new ArrayList<>(List.of(province)));
        }
        return new GeoData(provinceIdsByName, municipalitiesByProvince);
    }

    private static GeoData fromRows(List<Map<String, String>> provinces, List<Map<String, String>> municipalities) {
        Map<String, String> provinceNamesById = new HashMap<>();
        Map<String, String> provinceIdsByName = new HashMap<>();
        Map<String, List<String>> municipalitiesByProvince = new HashMap<>();
        for (Map<String, String> province : provinces) {
            String id = province.get("provincia_id");
            String name = LocationUtils.displayLocationText(province.get("nombre"));
            if (id != null && name != null && !name.isBlank()) {
                provinceNamesById.put(id, name);
                provinceIdsByName.put(name, id);
                municipalitiesByProvince.putIfAbsent(name, new ArrayList<>());
            }
        }
        for (Map<String, String> municipality : municipalities) {
            String provinceName = provinceNamesById.get(municipality.get("provincia_id"));
            String name = LocationUtils.displayLocationText(municipality.get("nombre"));
            if (provinceName != null && name != null && !name.isBlank()) {
                municipalitiesByProvince.get(provinceName).add(name);
            }
        }
        municipalitiesByProvince.values().forEach(values -> values.sort(String::compareToIgnoreCase));
        return new GeoData(provinceIdsByName, municipalitiesByProvince);
    }

    public record GeoData(Map<String, String> provinceIdsByName, Map<String, List<String>> municipalitiesByProvince) {
    }
}
