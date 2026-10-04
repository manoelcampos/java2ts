package io.github.manoelcampos.java2ts.parser.type;

import java.util.List;

/**
 * Identifies JDK types, which are never declared in TypeScript.
 * That includes interfaces that aren't useful for the frontend,
 * such as {@link java.io.Serializable}, {@link Comparable} and {@link Cloneable}.
 * @author Manoel Campos
 */
public final class JdkTypes {
    private static final List<String> JDK_PACKAGE_PREFIXES = List.of("java.", "javax.", "jdk.", "sun.", "com.sun.");

    private JdkTypes() {/**/}

    /**
     * {@return true if a class belongs to the JDK, false otherwise}
     * @param aClass the class to check
     */
    public static boolean isJdkType(final Class<?> aClass) {
        final String name = aClass.getName();
        return aClass.isPrimitive() || JDK_PACKAGE_PREFIXES.stream().anyMatch(name::startsWith);
    }
}
