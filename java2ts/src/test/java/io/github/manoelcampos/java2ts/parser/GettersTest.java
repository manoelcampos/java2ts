package io.github.manoelcampos.java2ts.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GettersTest {
    @SuppressWarnings("unused")
    public static class Sample {
        public String getName() { return ""; }
        public String getURL() { return ""; }
        public boolean isActive() { return true; }
        public Boolean isWrapped() { return true; }
        public void getNothing() {}
        public String getWithParam(final int x) { return ""; }
        public String get() { return ""; }
        public String getter() { return ""; }
        public String is() { return ""; }
        public String name() { return ""; }
    }

    @ParameterizedTest
    @CsvSource({
        "getName, true, name", "getURL, true, url", "isActive, true, active",
        "isWrapped, false, ", "getNothing, false, ", "get, false, ", "getter, false, ",
        "is, false, ", "name, false, ", "getClass, false, "
    })
    void identifiesGetters(final String methodName, final boolean getter, final String propertyName) throws NoSuchMethodException {
        final var method = Sample.class.getMethod(methodName);
        assertEquals(getter, Getters.isGetter(method));
        if (getter)
            assertEquals(propertyName, Getters.propertyName(method));
    }
}
