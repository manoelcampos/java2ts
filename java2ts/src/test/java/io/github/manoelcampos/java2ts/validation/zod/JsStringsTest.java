package io.github.manoelcampos.java2ts.validation.zod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsStringsTest {
    @Test
    void quotesAndEscapes() {
        assertEquals("\"a\\\"b\\\\c\\nd\\re\\tf\\u2028\"", JsStrings.quote("a\"b\\c\nd\re\tf" + (char) 0x2028));
    }

    @Test
    void quotesKeysOnlyWhenNeeded() {
        assertEquals("name", JsStrings.key("name"));
        assertEquals("\"first-name\"", JsStrings.key("first-name"));
    }
}
