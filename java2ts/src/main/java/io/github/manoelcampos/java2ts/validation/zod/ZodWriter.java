package io.github.manoelcampos.java2ts.validation.zod;

import io.github.manoelcampos.java2ts.validation.model.EnumSchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.ObjectSchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.ObjectSchemaDeclaration.TypeParameter;
import io.github.manoelcampos.java2ts.validation.model.SchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.SchemaProperty;
import io.github.manoelcampos.java2ts.validation.model.ValidationModel;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writes a {@link ValidationModel} as <a href="https://zod.dev">Zod 4</a> schemas,
 * which implement <a href="https://standardschema.dev">Standard Schema</a>.
 *
 * <p>Each schema is typed with the TypeScript type it validates (such as {@code z.ZodType<Person>}),
 * so that the TypeScript compiler fails if a schema doesn't match its type.
 * Generic types get schema factories, such as {@code PageSchema(PersonSchema)}.
 * Properties referencing other schemas are written as getters, so that schemas can be declared in any order
 * and can be recursive.</p>
 * @author Manoel Campos
 */
public final class ZodWriter {
    private static final String INDENT = "    ";
    private static final char BACKTICK = '`';

    /** A helper function returning the current local date as an ISO-8601 string. */
    private static final String LOCAL_DATE_HELPER =
        "/** Returns the current local date as an ISO-8601 string (yyyy-MM-dd), used to validate past and future dates. */\n" +
        "const %s = (): string => {\n" +
        "    const now = new Date();\n" +
        "    return " + BACKTICK + "${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, \"0\")}-${String(now.getDate()).padStart(2, \"0\")}" + BACKTICK + ";\n" +
        "};\n\n";

    /**
     * Creates a Zod writer.
     */
    public ZodWriter() {/**/}

    /**
     * {@return the TypeScript code with the schemas of a model}
     * @param model the model to write
     */
    public String write(final ValidationModel model) {
        final var formatter = new ZodTypeFormatter(model);
        final String declarations = model.declarations().stream()
                                         .map(declaration -> declaration(declaration, formatter, model))
                                         .collect(Collectors.joining("\n"));
        return imports(model) + config(model) + helpers(formatter) + declarations;
    }

    private static String imports(final ValidationModel model) {
        final var builder = new StringBuilder("import { z } from \"zod\";\n");
        final List<String> types = model.declarations().stream().map(SchemaDeclaration::typeName).toList();
        if (!types.isEmpty())
            builder.append("import type { %s } from %s;\n".formatted(String.join(", ", types), JsStrings.quote(model.declarationsModule())));

        model.customSchemasModule()
             .filter(module -> !model.customSchemaTypes().isEmpty())
             .ifPresent(module -> builder.append("import { %s } from %s;\n".formatted(customSchemaNames(model), JsStrings.quote(module))));

        return builder.append('\n').toString();
    }

    private static String customSchemaNames(final ValidationModel model) {
        return model.customSchemaTypes().stream().map(model::schemaName).collect(Collectors.joining(", "));
    }

    private static String config(final ValidationModel model) {
        return model.locale().map(locale -> "z.config(z.locales.%s());\n\n".formatted(ZodLocales.of(locale))).orElse("");
    }

    private static String helpers(final ZodTypeFormatter formatter) {
        if (!formatter.usesLocalDate())
            return "";

        return LOCAL_DATE_HELPER.formatted(ZodConstraintFormatter.LOCAL_DATE_FUNCTION);
    }

    private static String declaration(final SchemaDeclaration declaration, final ZodTypeFormatter formatter, final ValidationModel model) {
        final String schemaName = model.schemaName(declaration.typeName());
        return switch (declaration) {
            case EnumSchemaDeclaration enumDeclaration -> enumSchema(enumDeclaration, schemaName);
            case ObjectSchemaDeclaration object -> objectSchema(object, schemaName, formatter, model);
        };
    }

    private static String enumSchema(final EnumSchemaDeclaration declaration, final String schemaName) {
        final String schema = declaration.constants().isEmpty() ?
                "z.never()" :
                "z.enum([%s])".formatted(declaration.constants().stream().map(JsStrings::quote).collect(Collectors.joining(", ")));
        return "export const %s = %s satisfies z.ZodType<%s>;\n".formatted(schemaName, schema, declaration.typeName());
    }

    private static String objectSchema(
        final ObjectSchemaDeclaration declaration, final String schemaName,
        final ZodTypeFormatter formatter, final ValidationModel model)
    {
        final String properties = declaration.properties().stream()
                                             .map(property -> INDENT + property(property, formatter) + ",\n")
                                             .collect(Collectors.joining());
        final String object = "z.object({\n%s})".formatted(properties);
        final List<TypeParameter> params = declaration.typeParameters();
        if (params.isEmpty())
            return "export const %s: z.ZodType<%s> = %s;\n".formatted(schemaName, declaration.typeName(), object);

        final String typeParams = joinParams(params, TypeParameter::declaration);
        final String schemaParams = joinParams(params, param -> "%s: z.ZodType<%s>".formatted(model.schemaName(param.name()), param.name()));
        final String type = "%s<%s>".formatted(declaration.typeName(), joinParams(params, TypeParameter::name));
        return "export const %s = <%s>(%s): z.ZodType<%s> => %s;\n".formatted(schemaName, typeParams, schemaParams, type, object);
    }

    private static String joinParams(final List<TypeParameter> params, final Function<TypeParameter, String> mapper) {
        return params.stream().map(mapper).collect(Collectors.joining(", "));
    }

    /**
     * {@return the code of a property}, which is a getter when it references other schemas
     * (so that it works regardless of the schemas declaration order, even for recursive schemas)
     */
    private static String property(final SchemaProperty property, final ZodTypeFormatter formatter) {
        final String key = JsStrings.key(property.name());
        final String nullability = ZodTypeFormatter.nullability(property.acceptsNull(), property.acceptsUndefined(), property.questionMark());
        final String schema = formatter.format(property.type()) + nullability;
        final boolean referencesSchemas = property.type().referencedTypes().findAny().isPresent();
        return referencesSchemas ? "get %s() { return %s; }".formatted(key, schema) : "%s: %s".formatted(key, schema);
    }
}
