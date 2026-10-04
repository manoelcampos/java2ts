package io.github.manoelcampos.java2ts.scan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Lists the names of the classes inside classpath entries (directories and jar files),
 * without loading them.
 * @author Manoel Campos
 */
public final class ClasspathScanner {
    private static final String CLASS_EXTENSION = ".class";

    private ClasspathScanner() {/**/}

    /**
     * Lists the names of the classes inside a classpath entry.
     * @param entry a directory or jar file
     * @return the binary names of the classes (such as {@code com.company.Outer$Inner}),
     *         or an empty list if the entry is neither a directory nor a jar file
     */
    public static List<String> classNames(final Path entry) {
        if (Files.isDirectory(entry))
            return directoryClassNames(entry);

        final String fileName = entry.getFileName() == null ? "" : entry.getFileName().toString();
        return Files.isRegularFile(entry) && fileName.endsWith(".jar") ? jarClassNames(entry) : List.of();
    }

    private static List<String> directoryClassNames(final Path dir) {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(Files::isRegularFile)
                        .map(file -> dir.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/"))
                        .filter(ClasspathScanner::isClassFile)
                        .map(ClasspathScanner::toClassName)
                        .toList();
        } catch (final IOException e) {
            throw new UncheckedIOException("Error scanning classes in " + dir, e);
        }
    }

    private static List<String> jarClassNames(final Path jar) {
        try (var jarFile = new JarFile(jar.toFile())) {
            return jarFile.stream()
                          .map(JarEntry::getName)
                          .filter(ClasspathScanner::isClassFile)
                          .map(ClasspathScanner::toClassName)
                          .toList();
        } catch (final IOException e) {
            throw new UncheckedIOException("Error scanning classes in " + jar, e);
        }
    }

    private static boolean isClassFile(final String path) {
        return path.endsWith(CLASS_EXTENSION) && !path.endsWith("module-info.class")
               && !path.endsWith("package-info.class") && !path.startsWith("META-INF/");
    }

    private static String toClassName(final String path) {
        return path.substring(0, path.length() - CLASS_EXTENSION.length()).replace('/', '.');
    }
}
