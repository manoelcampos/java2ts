package io.github.manoelcampos.java2ts.javadoc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Finds Java source files inside source directories.
 * @author Manoel Campos
 */
final class JavaSourceFinder {
    private JavaSourceFinder() {/**/}

    /**
     * {@return all Java source files inside the given directories (excluding module-info.java),
     * ignoring directories that don't exist}
     * @param sourceRoots the source directories
     */
    static List<Path> find(final Collection<Path> sourceRoots) {
        return sourceRoots.stream().filter(Files::isDirectory).flatMap(JavaSourceFinder::find).toList();
    }

    private static Stream<Path> find(final Path sourceRoot) {
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            return files.filter(JavaSourceFinder::isJavaSource).toList().stream();
        } catch (final IOException e) {
            throw new UncheckedIOException("Error looking for Java sources in " + sourceRoot, e);
        }
    }

    private static boolean isJavaSource(final Path file) {
        final String name = String.valueOf(file.getFileName());
        return Files.isRegularFile(file) && name.endsWith(".java") && !name.equals("module-info.java");
    }
}
