package io.github.manoelcampos.java2ts.validation.model;

/**
 * The declaration of the schema for a TypeScript type.
 * @author Manoel Campos
 */
public sealed interface SchemaDeclaration permits ObjectSchemaDeclaration, EnumSchemaDeclaration {
    /**
     * {@return the name of the TypeScript type validated by the schema}
     */
    String typeName();
}
