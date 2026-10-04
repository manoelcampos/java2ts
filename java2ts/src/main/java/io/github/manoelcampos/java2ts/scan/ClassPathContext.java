package io.github.manoelcampos.java2ts.scan;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The classpath where classes to be converted are looked up.
 * @param classLoader the class loader used to load the classes
 * @param entries the classpath entries (directories and jar files) to scan for classes
 * @author Manoel Campos
 */
public record ClassPathContext(ClassLoader classLoader, List<Path> entries) {
    /**
     * Creates a {@link ClassPathContext}, validating the components and making immutable copies of collections.
     */
    public ClassPathContext {
        requireNonNull(classLoader);
        entries = List.copyOf(entries);
    }

    /**
     * {@return a context for the classpath of the running JVM}
     */
    public static ClassPathContext ofCurrentClassPath() {
        final List<Path> entries = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                         .filter(entry -> !entry.isBlank())
                                         .map(Path::of)
                                         .toList();
        return new ClassPathContext(Thread.currentThread().getContextClassLoader(), entries);
    }
}
