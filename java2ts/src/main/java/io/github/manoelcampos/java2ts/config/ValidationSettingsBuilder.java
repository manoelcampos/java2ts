package io.github.manoelcampos.java2ts.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds {@link ValidationSettings} objects, using the {@link Defaults} for the settings not set.
 * @author Manoel Campos
 */
public final class ValidationSettingsBuilder {
    private boolean enabled = Boolean.parseBoolean(Defaults.VALIDATION_ENABLED);
    private Optional<Path> outputFile = Optional.empty();
    private List<String> excludeClasses = List.of();
    private List<String> excludeClassPatterns = List.of();
    private Optional<String> customSchemasModule = Optional.empty();
    private String schemaNameSuffix = Defaults.SCHEMA_NAME_SUFFIX;
    private boolean zodConfig = Boolean.parseBoolean(Defaults.ZOD_CONFIG);
    private Optional<String> locale = Optional.empty();
    private Map<String, String> customTypeMappings = Map.of();

    ValidationSettingsBuilder() {/**/}

    /**
     * Sets if the validation file is generated.
     * @param enabled true to generate the file, false otherwise
     * @return this builder
     */
    public ValidationSettingsBuilder enabled(final boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the validation file to generate.
     * @param outputFile the file path; if not set, the file is created next to the TypeScript declarations file
     * @return this builder
     */
    public ValidationSettingsBuilder outputFile(final Path outputFile) {
        this.outputFile = Optional.of(outputFile);
        return this;
    }

    /**
     * Sets the classes whose schemas are written by hand.
     * @param excludeClasses fully qualified class names; their schemas are imported from the {@link #customSchemasModule(String)}
     * @return this builder
     */
    public ValidationSettingsBuilder excludeClasses(final List<String> excludeClasses) {
        this.excludeClasses = excludeClasses;
        return this;
    }

    /**
     * Sets patterns of the classes whose schemas are written by hand.
     * @param excludeClassPatterns class patterns; matching classes have their schemas imported from the {@link #customSchemasModule(String)}
     * @return this builder
     */
    public ValidationSettingsBuilder excludeClassPatterns(final List<String> excludeClassPatterns) {
        this.excludeClassPatterns = excludeClassPatterns;
        return this;
    }

    /**
     * Sets the module where hand-written schemas are imported from.
     * @param customSchemasModule a module specifier (such as {@code ./validation.custom}) written as is into the import
     * @return this builder
     */
    public ValidationSettingsBuilder customSchemasModule(final String customSchemasModule) {
        this.customSchemasModule = Optional.of(customSchemasModule);
        return this;
    }

    /**
     * Sets the suffix of the schema names.
     * @param schemaNameSuffix the suffix added to the TypeScript type name (such as {@code Schema} for {@code PersonSchema})
     * @return this builder
     */
    public ValidationSettingsBuilder schemaNameSuffix(final String schemaNameSuffix) {
        this.schemaNameSuffix = schemaNameSuffix;
        return this;
    }

    /**
     * Sets if the Zod configuration is written into the validation file.
     * @param zodConfig true to write the configuration (such as the {@link #locale(String)}), false to write just the schemas
     * @return this builder
     */
    public ValidationSettingsBuilder zodConfig(final boolean zodConfig) {
        this.zodConfig = zodConfig;
        return this;
    }

    /**
     * Sets the language of the validation messages.
     * @param locale a language tag (such as {@code pt-BR}); if not set, the Zod default (English) messages are used
     * @return this builder
     */
    public ValidationSettingsBuilder locale(final String locale) {
        this.locale = Optional.of(locale);
        return this;
    }

    /**
     * Sets custom conversions for specific Java types.
     * @param customTypeMappings a map where each key is a Java type name (that may include generic type arguments)
     *                           and the value is the schema expression it is converted to
     * @return this builder
     */
    public ValidationSettingsBuilder customTypeMappings(final Map<String, String> customTypeMappings) {
        this.customTypeMappings = customTypeMappings;
        return this;
    }

    /**
     * {@return a new {@link ValidationSettings} object with the values defined in this builder}
     */
    public ValidationSettings build() {
        return new ValidationSettings(
            enabled, outputFile, excludeClasses, excludeClassPatterns,
            customSchemasModule, schemaNameSuffix, zodConfig, locale, customTypeMappings);
    }
}
