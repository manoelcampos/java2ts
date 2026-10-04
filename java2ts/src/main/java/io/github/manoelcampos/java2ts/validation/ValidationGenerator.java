package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.parser.ParsedModel;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;
import io.github.manoelcampos.java2ts.validation.model.ValidationModel;
import io.github.manoelcampos.java2ts.validation.zod.ZodWriter;
import io.github.manoelcampos.java2ts.writer.FileHeader;
import io.github.manoelcampos.java2ts.writer.TextFiles;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Generates the validation file with the schemas for the classes converted to TypeScript (Facade pattern).
 * @author Manoel Campos
 */
public final class ValidationGenerator {
    private final Settings settings;
    private final Predicate<Class<?>> tsExclusion;

    /**
     * Creates a validation generator.
     * @param settings the conversion settings, including the {@link Settings#validation()} ones
     * @param tsExclusion checks if a class is excluded from the TypeScript conversion
     */
    public ValidationGenerator(final Settings settings, final Predicate<Class<?>> tsExclusion) {
        this.settings = settings;
        this.tsExclusion = tsExclusion;
    }

    /**
     * {@return the path of the validation file}
     */
    public Path outputFile() {
        return settings.validation().outputFile(settings.outputFile());
    }

    /**
     * {@return the code of the validation file, including the file header}
     * @param model the classes converted to TypeScript
     * @throws UnsupportedValidationException if any type or constraint can't be converted
     */
    public String generate(final ParsedModel model) {
        final String declarationsModule = ModuleSpecifier.of(outputFile(), settings.outputFile());
        final ValidationModel validationModel = new ValidationModelParser(settings, tsExclusion).parse(model, declarationsModule);
        return new FileHeader(!settings.noFileDate()).format() + new ZodWriter().write(validationModel);
    }

    /**
     * Writes the validation file.
     * @param model the classes converted to TypeScript
     * @return the path of the generated file
     * @throws UnsupportedValidationException if any type or constraint can't be converted
     */
    public Path generateFile(final ParsedModel model) {
        final Path file = outputFile();
        TextFiles.write(file, generate(model));
        return file;
    }
}
