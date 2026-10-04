package io.github.manoelcampos.java2ts.ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsDocTest {
    @Test
    void formatsMultiLineComments() {
        assertEquals("  /**\n   * Line 1\n   *\n   * Line 2 *\\/\n   */\n", JsDoc.format("  Line 1\n\n   Line 2 */ ", "  "));
    }

    @Test
    void returnsEmptyForBlankComments() {
        assertEquals("", JsDoc.format(" ", ""));
        assertEquals("", JsDoc.format(null, ""));
    }
}
