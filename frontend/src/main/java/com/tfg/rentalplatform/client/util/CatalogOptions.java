package com.tfg.rentalplatform.client.util;

import java.util.List;

public final class CatalogOptions {

    public static final List<String> ITEM_CATEGORIES = List.of(
            "Herramientas",
            "Electronica",
            "Audio",
            "Deporte",
            "Camping",
            "Infantil",
            "Hogar",
            "Otros"
    );

    public static final List<String> FALLBACK_PROVINCES = List.of(
            "A Coruna",
            "Albacete",
            "Alicante/Alacant",
            "Almeria",
            "Alava",
            "Asturias",
            "Avila",
            "Badajoz",
            "Balears, Illes",
            "Barcelona",
            "Bizkaia",
            "Burgos",
            "Caceres",
            "Cadiz",
            "Cantabria",
            "Castellon/Castello",
            "Ceuta",
            "Ciudad Real",
            "Cordoba",
            "Cuenca",
            "Girona",
            "Gipuzkoa",
            "Granada",
            "Guadalajara",
            "Huelva",
            "Huesca",
            "Jaen",
            "Leon",
            "Lleida",
            "Lugo",
            "Madrid",
            "Malaga",
            "Melilla",
            "Murcia",
            "Navarra",
            "Ourense",
            "Palencia",
            "Palmas, Las",
            "Pontevedra",
            "Rioja, La",
            "Salamanca",
            "Santa Cruz de Tenerife",
            "Segovia",
            "Sevilla",
            "Soria",
            "Tarragona",
            "Teruel",
            "Toledo",
            "Valencia/Valencia",
            "Valladolid",
            "Zamora",
            "Zaragoza"
    );

    private CatalogOptions() {
    }

    public static String displayCategory(String category) {
        return switch (category == null ? "" : category) {
            case "Electronica" -> "Electronica";
            default -> category == null ? "" : category;
        };
    }
}
