package io.github.manoelcampos.java2ts.writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes generated text files.
 * @author Manoel Campos
 */
public final class TextFiles {
    private TextFiles() {/**/}

    /**
     * Writes a text into a UTF-8 file, creating parent directories if needed.
     * @param file the file to write
     * @param content the text to write
     * @throws UncheckedIOException if the file can't be written
     */
    public static void write(final Path file, final String content) {
        try {
            final Path parent = file.toAbsolutePath().getParent();
            if (parent != null)
                Files.createDirectories(parent);

            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Error writing file " + file, e);
        }
    }
}
