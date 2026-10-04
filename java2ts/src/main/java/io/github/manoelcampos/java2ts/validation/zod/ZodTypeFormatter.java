package io.github.manoelcampos.java2ts.validation.zod;

import io.github.manoelcampos.java2ts.validation.model.ArraySchema;
import io.github.manoelcampos.java2ts.validation.model.BooleanConstraint;
import io.github.manoelcampos.java2ts.validation.model.Constraint;
import io.github.manoelcampos.java2ts.validation.model.CustomSchema;
import io.github.manoelcampos.java2ts.validation.model.DateSchema;
import io.github.manoelcampos.java2ts.validation.model.EmailConstraint;
import io.github.manoelcampos.java2ts.validation.model.NullableSchema;
import io.github.manoelcampos.java2ts.validation.model.RecordSchema;
import io.github.manoelcampos.java2ts.validation.model.ReferenceSchema;
import io.github.manoelcampos.java2ts.validation.model.ScalarSchema;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;
import io.github.manoelcampos.java2ts.validation.model.TypeParameterSchema;
import io.github.manoelcampos.java2ts.validation.model.UrlConstraint;
import io.github.manoelcampos.java2ts.validation.model.ValidationModel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writes {@link SchemaType}s as Zod 4 schema expressions, such as {@code z.array(z.string().min(1))}.
 * @author Manoel Campos
 */
final class ZodTypeFormatter {
    private final ValidationModel model;
    private final ZodConstraintFormatter constraintFormatter;

    /**
     * Creates a formatter.
     * @param model the model whose schemas are written, which defines the schema names
     */
    ZodTypeFormatter(final ValidationModel model) {
        this.model = model;
        this.constraintFormatter = new ZodConstraintFormatter(model.locale().isEmpty());
    }

    /**
     * {@return true if any schema written so far uses the {@link ZodConstraintFormatter#LOCAL_DATE_FUNCTION} helper, false otherwise}
     */
    boolean usesLocalDate() {
        return constraintFormatter.usesLocalDate();
    }

    /**
     * {@return the Zod expression for a schema}
     * @param type the schema to write
     */
    String format(final SchemaType type) {
        return switch (type) {
            case ScalarSchema scalar -> scalar(scalar);
            case DateSchema date -> date(date) + constraintFormatter.format(date.constraints(), date);
            case ArraySchema array -> "z.array(%s)".formatted(format(array.element())) + constraintFormatter.format(array.constraints(), array);
            case RecordSchema record -> record(record);
            case ReferenceSchema reference -> reference(reference);
            case TypeParameterSchema parameter -> model.schemaName(parameter.name());
            case CustomSchema custom -> custom.expression();
            case NullableSchema nullable -> format(nullable.type()) + nullability(nullable.acceptsNull(), nullable.acceptsUndefined(), false);
        };
    }

    /**
     * {@return the method calls that make a schema accept null and/or undefined}
     * @param acceptsNull if null is accepted
     * @param acceptsUndefined if undefined is accepted
     * @param optionalKey if the value is an object property that may be absent
     *                    (otherwise, the property must be present even when undefined is accepted)
     */
    static String nullability(final boolean acceptsNull, final boolean acceptsUndefined, final boolean optionalKey) {
        if (optionalKey)
            return acceptsNull ? ".nullish()" : ".optional()";

        final String nullable = acceptsNull ? ".nullable()" : "";
        final String undefinable = acceptsUndefined ? ".or(z.undefined())" : "";
        return nullable + undefinable;
    }

    /**
     * {@return the expression for a scalar}, where some constraints replace the base schema
     * (such as {@code z.email()} instead of {@code z.string()})
     */
    private String scalar(final ScalarSchema scalar) {
        final List<Constraint> constraints = scalar.constraints();
        final Optional<String> replacement = constraints.stream().map(ZodTypeFormatter::baseReplacement).flatMap(Optional::stream).findFirst();
        return replacement.orElseGet(() -> base(scalar)) + constraintFormatter.format(constraints, scalar);
    }

    private static Optional<String> baseReplacement(final Constraint constraint) {
        final Function<Optional<String>, String> args = message -> message.map(ZodConstraintFormatter::options).orElse("");
        return switch (constraint) {
            case EmailConstraint email -> Optional.of("z.email(%s)".formatted(args.apply(email.message())));
            case UrlConstraint url -> Optional.of("z.url(%s)".formatted(args.apply(url.message())));
            case BooleanConstraint bool -> Optional.of("z.literal(%s%s)".formatted(bool.value(), bool.message().map(msg -> ", " + ZodConstraintFormatter.options(msg)).orElse("")));
            default -> Optional.empty();
        };
    }

    private static String base(final ScalarSchema scalar) {
        return switch (scalar.kind()) {
            case ANY -> "z.any()";
            case VOID -> "z.undefined()";
            case BOOLEAN -> "z.boolean()";
            case INTEGER -> "z.int()";
            case DECIMAL -> "z.number()";
            case CHAR -> "z.string().length(1)";
            case UUID -> "z.uuid()";
            case URL -> "z.url()";
            case BYTES -> "z.base64()";
            case STRING, URI, DURATION, PERIOD, ZONE_ID -> "z.string()";
        };
    }

    private static String date(final DateSchema date) {
        return switch (date.mapping()) {
            case asDate -> "z.coerce.date()";
            case asNumber -> "z.number()";
            case asString -> switch (date.kind()) {
                case LOCAL_DATE -> "z.iso.date()";
                case LOCAL_DATE_TIME -> "z.iso.datetime({ local: true })";
                case LOCAL_TIME -> "z.iso.time()";
                case INSTANT -> "z.iso.datetime()";
                case OFFSET_DATE_TIME -> "z.iso.datetime({ offset: true })";
                case OTHER -> "z.string()";
            };
        };
    }

    private String record(final RecordSchema record) {
        final String value = format(record.value());
        return switch (record.keyKind()) {
            case STRING -> "z.record(z.string(), %s)".formatted(value);
            case NUMBER -> "z.record(z.number(), %s)".formatted(value);
            case ENUM -> "z.partialRecord(%s, %s)".formatted(format(record.key()), value);
        };
    }

    private String reference(final ReferenceSchema reference) {
        final String name = model.schemaName(reference.typeName());
        if (reference.typeArguments().isEmpty())
            return name;

        return "%s(%s)".formatted(name, reference.typeArguments().stream().map(this::format).collect(Collectors.joining(", ")));
    }
}
