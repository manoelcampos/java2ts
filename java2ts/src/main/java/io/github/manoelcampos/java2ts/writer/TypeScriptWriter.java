package io.github.manoelcampos.java2ts.writer;

import io.github.manoelcampos.java2ts.ts.TsModel;

import java.nio.file.Path;

/**
 * Writes a {@link TsModel} as TypeScript code into a single file.
 * @param header the comment written at the beginning of the file
 * @author Manoel Campos
 */
public record TypeScriptWriter(FileHeader header) {
    /**
     * {@return the TypeScript code for a model, including the file header}
     * @param model the model to convert to TypeScript code
     */
    public String toTypeScript(final TsModel model) {
        return header.format() + model.format();
    }

    /**
     * Writes the TypeScript code of a model into a file, creating parent directories if needed.
     * @param model the model to write
     * @param file the file to write
     */
    public void write(final TsModel model, final Path file) {
        TextFiles.write(file, toTypeScript(model));
    }
}
