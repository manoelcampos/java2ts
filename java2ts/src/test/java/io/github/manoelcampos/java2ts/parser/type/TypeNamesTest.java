package io.github.manoelcampos.java2ts.parser.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeNamesTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        java.util.List<java.lang.String>                         | java.util.List<java.lang.String>
        java.util.List[java.lang.String]                         | java.util.List<java.lang.String>
        java.util.Map[ java.lang.String, java.util.List[Long] ]  | java.util.Map<java.lang.String,java.util.List<Long>>
        Class[T1[T2], T3]                                        | Class<T1<T2>,T3>
        java.lang.String[]                                       | java.lang.String[]
        java.util.List[java.lang.String[]]                       | java.util.List<java.lang.String[]>
        com.app.Outer$Inner                                      | com.app.Outer.Inner
        ]                                                        | ]
        """)
    void normalizes(final String typeName, final String expected) {
        assertEquals(expected, TypeNames.normalize(typeName));
    }
}
