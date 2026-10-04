package io.github.manoelcampos.java2ts.parser.type;

/**
 * Defines the TypeScript names for Java classes.
 * @author Manoel Campos
 */
public final class TsNames {
    private TsNames() {/**/}

    /**
     * {@return the TypeScript name for a Java class, which is its simple name}
     * Nested classes also use just their simple names (such as {@code Inner} for {@code Outer.Inner}).
     * @param aClass the class to get the name
     */
    public static String of(final Class<?> aClass) {
        return aClass.getSimpleName();
    }
}
