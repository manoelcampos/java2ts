package io.github.manoelcampos.java2ts.validation.zod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZodLocalesTest {
    @ParameterizedTest
    @CsvSource(textBlock = """
        pt-BR, ptBR
        pt_BR, ptBR
        pt-PT, pt
        pt,    pt
        en-US, en
        zh-CN, zhCN
        fr-CA, frCA
        fr-FR, fr
        """)
    void convertsLanguageTagToZodLocale(final String tag, final String expected) {
        assertEquals(expected, ZodLocales.of(tag));
    }

    @Test
    void failsForInvalidTag() {
        assertThrows(IllegalArgumentException.class, () -> ZodLocales.of("!"));
    }
}
