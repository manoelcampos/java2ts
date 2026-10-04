package io.github.manoelcampos.java2ts.parser.type;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomTypeMappingsTest {
    @Test
    void parsesMappings() {
        final var expected = Map.of("java.util.List[java.math.BigDecimal]", "number[]", "Foo", "{ [k: string]: number }");
        assertEquals(expected, CustomTypeMappings.parse(List.of("java.util.List[java.math.BigDecimal] : number[]", "Foo:{ [k: string]: number }")));
    }

    @Test
    void failsForInvalidMappings() {
        assertThrows(IllegalArgumentException.class, () -> CustomTypeMappings.parse(List.of("noSeparator")));
        assertThrows(IllegalArgumentException.class, () -> CustomTypeMappings.parse(List.of(":number")));
        assertThrows(IllegalArgumentException.class, () -> CustomTypeMappings.parse(List.of("Foo:")));
    }
}
