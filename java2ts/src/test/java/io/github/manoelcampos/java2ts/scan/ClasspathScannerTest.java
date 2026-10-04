package io.github.manoelcampos.java2ts.scan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathScannerTest {
    @Test
    void listsClassesInDirectory(@TempDir final Path dir) throws IOException {
        Files.createDirectories(dir.resolve("com/app/META-INF"));
        Files.createFile(dir.resolve("com/app/Person.class"));
        Files.createFile(dir.resolve("com/app/Person$Inner.class"));
        Files.createFile(dir.resolve("com/app/package-info.class"));
        Files.createFile(dir.resolve("module-info.class"));
        Files.createFile(dir.resolve("com/app/readme.txt"));

        final var names = ClasspathScanner.classNames(dir);
        assertEquals(2, names.size());
        assertTrue(names.containsAll(java.util.List.of("com.app.Person", "com.app.Person$Inner")));
    }

    @Test
    void listsClassesInJar(@TempDir final Path dir) throws IOException {
        final Path jar = dir.resolve("lib.jar");
        try (var out = new JarOutputStream(Files.newOutputStream(jar))) {
            for (final String entry : new String[]{"com/app/Person.class", "META-INF/versions/9/X.class", "com/app/data.txt"}) {
                out.putNextEntry(new JarEntry(entry));
                out.closeEntry();
            }
        }

        assertEquals(java.util.List.of("com.app.Person"), ClasspathScanner.classNames(jar));
    }

    @Test
    void returnsEmptyListForOtherFiles(@TempDir final Path dir) throws IOException {
        assertTrue(ClasspathScanner.classNames(Files.createFile(dir.resolve("x.txt"))).isEmpty());
        assertTrue(ClasspathScanner.classNames(dir.resolve("missing.jar")).isEmpty());
    }

    @Test
    void failsForInvalidJar(@TempDir final Path dir) throws IOException {
        final Path jar = Files.writeString(dir.resolve("invalid.jar"), "not a jar");
        assertThrows(UncheckedIOException.class, () -> ClasspathScanner.classNames(jar));
    }
}
