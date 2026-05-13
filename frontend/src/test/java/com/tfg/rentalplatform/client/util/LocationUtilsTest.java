package com.tfg.rentalplatform.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationUtilsTest {

    @Test
    void normalizesAccentAndEncodingVariantsForFilters() {
        assertEquals("malaga", LocationUtils.locationKey("M\u00e1laga"));
        assertEquals("malaga", LocationUtils.locationKey("M?laga"));
        assertEquals("valencia", LocationUtils.locationKey("Val\u00e8ncia"));
        assertEquals("valencia", LocationUtils.locationKey("Val?ncia"));
    }

    @Test
    void displaysKnownLocationsWithoutBrokenCharacters() {
        assertEquals("Malaga", LocationUtils.displayLocationText("M?laga"));
        assertEquals("Valencia", LocationUtils.displayLocationText("Val\u00e8ncia / Valencia"));
        assertEquals("Cordoba", LocationUtils.displayLocationText("C\u00f3rdoba"));
    }
}
