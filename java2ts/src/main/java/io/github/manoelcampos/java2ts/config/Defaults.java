package io.github.manoelcampos.java2ts.config;

import java.util.Set;

/**
 * Default values for the {@link Settings}, shared by the {@link SettingsBuilder} and the Maven plugin
 * (which requires the values as constant strings), so that they're defined in a single place.
 * The defaults target projects that serialize objects to JSON using Jackson.
 * @author Manoel Campos
 */
public final class Defaults {
    /** Modern frontends usually import types from .ts files. */
    public static final String OUTPUT_FILE_TYPE = "implementationFile";

    /** Nullable types are written as inline unions, such as {@code string | null}. */
    public static final String NULLABILITY_DEFINITION = "nullInlineUnion";

    /** Jackson writes null properties by default, so optional properties may be either absent or null. */
    public static final String OPTIONAL_PROPERTIES_DECLARATION = "questionMarkAndNullableType";

    /** Jackson writes {@code java.time} types as ISO-8601 strings (when WRITE_DATES_AS_TIMESTAMPS is disabled). */
    public static final String MAP_DATE = "asString";

    /** Record components and final fields are declared as read-only properties. */
    public static final String READONLY_PROPERTIES = "true";

    /** Not including the date avoids changing the generated file when nothing else changed. */
    public static final String NO_FILE_DATE = "true";

    /**
     * JavaDocs are extracted from the project sources and copied to the TypeScript file by default,
     * so that the frontend has the same documentation as the backend.
     */
    public static final String JAVADOC = "true";

    /** The default version of the xml-doclet used to extract JavaDocs. */
    public static final String XML_DOCLET_VERSION = "2.0.3";

    /** The most common nullable annotations. */
    public static final Set<String> NULLABLE_ANNOTATIONS = Set.of(
        "org.jspecify.annotations.Nullable",
        "org.jetbrains.annotations.Nullable",
        "jakarta.annotation.Nullable",
        "javax.annotation.Nullable"
    );

    /**
     * The most common annotations indicating a value is required.
     * Annotations with CLASS retention (such as {@code lombok.NonNull}) are supported too,
     * since they're read directly from class files.
     */
    public static final Set<String> REQUIRED_ANNOTATIONS = Set.of(
        "jakarta.validation.constraints.NotNull",
        "jakarta.validation.constraints.NotBlank",
        "jakarta.validation.constraints.NotEmpty",
        "javax.validation.constraints.NotNull",
        "javax.validation.constraints.NotBlank",
        "javax.validation.constraints.NotEmpty",
        "org.jspecify.annotations.NonNull",
        "org.jetbrains.annotations.NotNull",
        "lombok.NonNull"
    );

    private Defaults() {/**/}
}
