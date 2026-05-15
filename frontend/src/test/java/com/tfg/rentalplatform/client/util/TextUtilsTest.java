package com.tfg.rentalplatform.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextUtilsTest {

    @Test
    void cleansCorruptedSpanishWordsUsedInTheUi() {
        assertEquals("administracion", TextUtils.cleanDisplayText("administraci??n"));
        assertEquals("Contenido enga\u00f1oso - Reportado por Ana", TextUtils.cleanDisplayText("Contenido enga?oso Ã‚Â· Reportado por Ana"));
    }

    @Test
    void createsReadableFallbacksAndPreviews() {
        assertEquals("", TextUtils.nullableString("null"));
        assertEquals("Sin descripcion disponible.", TextUtils.previewText("   ", 20));
        assertEquals("Texto largo...", TextUtils.previewText("Texto largo para probar", 13));
    }

    @Test
    void wrapsVeryLongWordsWithoutChangingVisibleText() {
        String wrapped = TextUtils.wrapLongWords("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        assertTrue(wrapped.contains("\u200B"));
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapped.replace("\u200B", ""));
    }
}
