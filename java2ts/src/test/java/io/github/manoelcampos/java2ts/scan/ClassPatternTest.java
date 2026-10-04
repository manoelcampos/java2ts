package io.github.manoelcampos.java2ts.scan;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassPatternTest {
    @ParameterizedTest
    @CsvSource({
        "com.app.model.**, com.app.model.Person, true",
        "com.app.model.**, com.app.model.sub.Person, true",
        "com.app.model.**, com.app.model.Person$Inner, true",
        "com.app.model.**, com.app.other.Person, false",
        "com.app.model.*, com.app.model.Person, true",
        "com.app.model.*, com.app.model.sub.Person, false",
        "com.app.model.*, com.app.model.Person$Inner, false",
        "com.app.*.dto.*DTO, com.app.x.dto.PersonDTO, true",
        "com.app.*.dto.*DTO, com.app.x.dto.Person, false",
        "com.app.Person, com.app.Person, true",
        "com.app.Person, comXapp.Person, false"
    })
    void matches(final String glob, final String className, final boolean expected) {
        assertEquals(expected, new ClassPattern(glob).matches(className));
    }
}
