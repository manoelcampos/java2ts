package io.github.manoelcampos.java2ts.validation.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.requireNonNull;

/**
 * All the schemas to be written into the validation file, independent of any validation library.
 * Declarations are sorted by name, so that the generated file only changes when the Java classes change.
 *
 * @param declarations the schema declarations
 * @param declarationsModule the module specifier of the TypeScript declarations file, from where types are imported
 * @param customSchemaTypes names of the TypeScript types whose schemas are written by hand
 *                          and referenced by the generated schemas
 * @param customSchemasModule the module specifier from where hand-written schemas are imported
 * @param schemaNameSuffix the suffix added to TypeScript type names to define schema names
 * @param locale the language tag defining the language of validation messages,
 *               or an empty Optional to use the default messages or to leave the configuration to the application
 * @author Manoel Campos
 */
public record ValidationModel(
    List<SchemaDeclaration> declarations, String declarationsModule,
    Set<String> customSchemaTypes, Optional<String> customSchemasModule,
    String schemaNameSuffix, Optional<String> locale)
{
    /**
     * Creates a {@link ValidationModel}, validating the components and making immutable copies of collections.
     */
    public ValidationModel {
        declarations = declarations.stream().sorted(Comparator.comparing(SchemaDeclaration::typeName)).toList();
        requireNonNull(declarationsModule);
        customSchemaTypes = new TreeSet<>(customSchemaTypes);
        requireNonNull(customSchemasModule);
        requireNonNull(schemaNameSuffix);
        requireNonNull(locale);
    }

    /**
     * {@return the name of the schema for a TypeScript type (or type parameter)}
     * @param typeName the TypeScript type name
     */
    public String schemaName(final String typeName) {
        return typeName + schemaNameSuffix;
    }
}
