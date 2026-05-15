package com.tfg.rentalplatform.client.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormatUtilsTest {

    @Test
    void convertsNumbersAndInvalidValuesSafely() {
        assertEquals(7L, FormatUtils.toLongOrNull("7"));
        assertNull(FormatUtils.toLongOrNull("abc"));
        assertEquals(12.5, FormatUtils.toDouble("12.5"));
        assertEquals(0, FormatUtils.toInt("no-numero"));
    }

    @Test
    void formatsMoneyAndDatesForDisplay() {
        assertEquals("12.50 EUR", FormatUtils.formatMoney(new BigDecimal("12.5")));
        assertEquals("9.99 EUR", FormatUtils.formatMoney("9,99"));
        assertEquals("-- EUR", FormatUtils.formatMoney(null));
        assertEquals("06/05/2026", FormatUtils.formatDate("2026-05-06"));
        assertEquals("Del 06/05/2026 al 08/05/2026", FormatUtils.formatReservationDates(Map.of(
                "startDate", "2026-05-06",
                "endDate", "2026-05-08"
        )));
    }
}
