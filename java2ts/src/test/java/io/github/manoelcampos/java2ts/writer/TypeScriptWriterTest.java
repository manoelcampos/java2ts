package io.github.manoelcampos.java2ts.writer;

import io.github.manoelcampos.java2ts.ts.TsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeScriptWriterTest {
    @Test
    void failsWhenFileCannotBeWritten(@TempDir final Path dir) throws IOException {
        final Path file = Files.createDirectory(dir.resolve("models.ts"));
        final var writer = new TypeScriptWriter(new FileHeader(false));
        assertThrows(UncheckedIOException.class, () -> writer.write(new TsModel(List.of()), file));
    }
}
