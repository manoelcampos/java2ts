package io.github.manoelcampos.java2ts.scan;

import java.util.Optional;

/**
 * Loads classes by name, without initializing them.
 * @param classLoader the class loader used to load classes
 * @author Manoel Campos
 */
record ClassLoading(ClassLoader classLoader) {
    /**
     * {@return the loaded class}
     * @param className the fully qualified (binary) name of the class
     * @throws IllegalArgumentException if the class is not found
     */
    Class<?> load(final String className) {
        return tryLoad(className).orElseThrow(() -> new IllegalArgumentException("Class not found: " + className));
    }

    /**
     * {@return an Optional with the loaded class, or an empty Optional if the class cannot be loaded}
     * @param className the fully qualified (binary) name of the class
     */
    Optional<Class<?>> tryLoad(final String className) {
        try {
            return Optional.of(Class.forName(className, false, classLoader));
        } catch (final ClassNotFoundException | LinkageError e) {
            return Optional.empty();
        }
    }
}
