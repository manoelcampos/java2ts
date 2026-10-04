package io.github.manoelcampos.java2ts.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Settings to generate a file with <a href="https://zod.dev">Zod 4</a> validation schemas for the TypeScript declarations,
 * built from Java nullability and Jakarta Bean Validation annotations.
 * Zod 4 schemas implement <a href="https://standardschema.dev">Standard Schema</a>, so they can be used
 * by any library that accepts it (such as TanStack Form, React Hook Form and Mantine 9).
 * Use {@link #builder()} to create an instance with default values for the settings not set.
 *
 * @param enabled if the validation file is generated
 * @param outputFile the validation file to generate. If empty, the {@link Defaults#VALIDATION_FILE_NAME}
 *                   is created in the same directory as the TypeScript declarations file
 * @param excludeClasses fully qualified names of classes whose schemas are written by hand
 * @param excludeClassPatterns patterns of classes whose schemas are written by hand
 * @param customSchemasModule the module where hand-written schemas (of excluded classes) are imported from,
 *                            such as {@code ./validation.custom}
 * @param schemaNameSuffix the suffix added to the TypeScript type name to define the schema name
 *                         (such as {@code PersonSchema})
 * @param zodConfig if true, the Zod configuration (such as the {@code locale}) is written into the file.
 *                  If false, just the schemas are written, so that the application configures Zod by itself
 * @param locale the language tag (such as {@code pt-BR}) defining the language of the validation messages.
 *               If empty, the Zod default (English) messages are used
 * @param customTypeMappings maps a Java type name (with the same syntax of {@link Settings#customTypeMappings()})
 *                           to the schema expression it must be converted to
 * @author Manoel Campos
 */
public record ValidationSettings(
    boolean enabled,
    Optional<Path> outputFile,
    List<String> excludeClasses,
    List<String> excludeClassPatterns,
    Optional<String> customSchemasModule,
    String schemaNameSuffix,
    boolean zodConfig,
    Optional<String> locale,
    Map<String, String> customTypeMappings)
{
    /** The default settings, where the validation file is not generated. */
    public static final ValidationSettings DEFAULT = builder().build();

    /**
     * Creates a {@link ValidationSettings}, validating the components and making immutable copies of collections.
     */
    public ValidationSettings {
        requireNonNull(outputFile, "outputFile");
        requireNonNull(customSchemasModule, "customSchemasModule");
        requireNonNull(schemaNameSuffix, "schemaNameSuffix");
        requireNonNull(locale, "locale");
        excludeClasses = List.copyOf(excludeClasses);
        excludeClassPatterns = List.copyOf(excludeClassPatterns);
        customTypeMappings = Map.copyOf(customTypeMappings);
    }

    /**
     * {@return a new builder to create a {@link ValidationSettings} object with default values}
     */
    public static ValidationSettingsBuilder builder() {
        return new ValidationSettingsBuilder();
    }

    /**
     * {@return the validation file to generate}
     * @param declarationsFile the TypeScript declarations file, used to define the default validation file location
     */
    public Path outputFile(final Path declarationsFile) {
        return outputFile.orElseGet(() -> declarationsFile.resolveSibling(Defaults.VALIDATION_FILE_NAME));
    }
}
