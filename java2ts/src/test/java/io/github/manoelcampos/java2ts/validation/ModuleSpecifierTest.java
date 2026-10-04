package io.github.manoelcampos.java2ts.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleSpecifierTest {
    @ParameterizedTest
    @CsvSource(textBlock = """
        front/validation.ts,  front/models.ts,         ./models
        front/validation.ts,  front/models.d.ts,       ./models
        front/validation.ts,  front/model/models.ts,   ./model/models
        front/a/validation.ts, front/models.generated.ts, ../models.generated
        front/validation.ts,  front/models.js,         ./models.js
        """)
    void createsRelativeSpecifierWithoutExtension(final String importing, final String imported, final String expected) {
        assertEquals(expected, ModuleSpecifier.of(Path.of(importing), Path.of(imported)));
    }
}
