package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsRegexTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        [a-z]+          |                              | /^(?:[a-z]+)$/
        a/b\\/c         |                              | /^(?:a\\/b\\/c)$/
        x               | CASE_INSENSITIVE,DOTALL       | /^(?:x)$/is
        x               | MULTILINE,UNICODE_CASE        | /^(?:x)$/m
        \\p{Lu}+        |                              | /^(?:\\p{Lu}+)$/u
        a\\++           |                              | /^(?:a\\++)$/
        """)
    void convertsToJavaScriptLiteral(final String regex, final String flags, final String expected) {
        final Set<String> flagSet = flags == null ? Set.of() : Set.of(flags.split(","));
        assertEquals(expected, JsRegex.of(regex, flagSet));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\\Aabc", "abc\\z", "(?>a)", "(?i)abc", "a++", "a{2}+"})
    void failsForJavaOnlySyntax(final String regex) {
        assertThrows(UnsupportedValidationException.class, () -> JsRegex.of(regex, Set.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMENTS", "CANON_EQ"})
    void failsForUnsupportedFlags(final String flag) {
        assertThrows(UnsupportedValidationException.class, () -> JsRegex.of("a", Set.of(flag)));
    }
}
