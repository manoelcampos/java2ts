package io.github.manoelcampos.java2ts.maven;

import io.github.manoelcampos.java2ts.config.Defaults;
import io.github.manoelcampos.java2ts.config.ValidationSettings;
import io.github.manoelcampos.java2ts.config.ValidationSettingsBuilder;
import io.github.manoelcampos.java2ts.parser.type.CustomTypeMappings;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * The {@code <validation>} group of the plugin configuration, where all the settings to generate
 * validation schemas live. Maven sets the fields directly, so they're mutable.
 * Check {@link ValidationSettings} for details about each parameter.
 * @author Manoel Campos
 */
public class ValidationParameters {
    boolean enabled = Boolean.parseBoolean(Defaults.VALIDATION_ENABLED);
    @Nullable File outputFile;
    @Nullable List<String> excludeClasses;
    @Nullable List<String> excludeClassPatterns;
    @Nullable String customSchemasModule;
    String schemaNameSuffix = Defaults.SCHEMA_NAME_SUFFIX;
    boolean zodConfig = Boolean.parseBoolean(Defaults.ZOD_CONFIG);
    @Nullable String locale;

    /** Mappings in the format javaType:schemaExpression, such as java.math.BigDecimal:z.string() */
    @Nullable List<String> customTypeMappings;

    /**
     * Creates a {@link ValidationParameters} with default values (instantiated by Maven).
     */
    public ValidationParameters() {/**/}

    /**
     * {@return the {@link ValidationSettings} with the values of these parameters}
     */
    ValidationSettings toSettings() {
        final ValidationSettingsBuilder builder = ValidationSettings.builder()
            .enabled(enabled)
            .excludeClasses(orEmpty(excludeClasses))
            .excludeClassPatterns(orEmpty(excludeClassPatterns))
            .schemaNameSuffix(schemaNameSuffix)
            .zodConfig(zodConfig)
            .customTypeMappings(CustomTypeMappings.parse(orEmpty(customTypeMappings)));

        if (outputFile != null)
            builder.outputFile(outputFile.toPath());
        if (customSchemasModule != null)
            builder.customSchemasModule(customSchemasModule);
        if (locale != null)
            builder.locale(locale);

        return builder.build();
    }

    private static List<String> orEmpty(final @Nullable List<String> list) {
        return list == null ? List.of() : list;
    }
}
