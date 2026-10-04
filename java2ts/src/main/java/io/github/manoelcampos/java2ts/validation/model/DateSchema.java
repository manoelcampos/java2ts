package io.github.manoelcampos.java2ts.validation.model;

import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.parser.type.DateKind;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * The schema of a date/time value.
 * @param kind the kind of the date/time
 * @param mapping how the date/time is represented in TypeScript
 * @param constraints the constraints of the value
 * @author Manoel Campos
 */
public record DateSchema(DateKind kind, DateMapping mapping, List<Constraint> constraints) implements ConstrainedSchema {
    /** Kinds of dates represented as strings that can be compared with the current date/time. */
    private static final Set<DateKind> COMPARABLE_STRINGS =
        Set.of(DateKind.LOCAL_DATE, DateKind.LOCAL_DATE_TIME, DateKind.INSTANT, DateKind.OFFSET_DATE_TIME);

    /**
     * Creates a {@link DateSchema}, validating the components and making immutable copies of collections.
     */
    public DateSchema {
        requireNonNull(kind);
        requireNonNull(mapping);
        constraints = List.copyOf(constraints);
    }

    /**
     * Creates a {@link DateSchema} without constraints.
     * @param kind the kind of the date/time
     * @param mapping how the date/time is represented in TypeScript
     */
    public DateSchema(final DateKind kind, final DateMapping mapping) {
        this(kind, mapping, List.of());
    }

    /**
     * {@inheritDoc}
     * Dates can be compared with the current date when they're represented as {@code Date} objects,
     * or as strings that include a date.
     */
    @Override
    public Optional<ConstraintTarget> constraintTarget() {
        final boolean comparable = switch (mapping) {
            case asDate -> kind != DateKind.LOCAL_TIME && kind != DateKind.OTHER;
            case asString -> COMPARABLE_STRINGS.contains(kind);
            case asNumber -> false;
        };

        return comparable ? Optional.of(ConstraintTarget.TEMPORAL) : Optional.empty();
    }

    @Override
    public String describe() {
        return "date/time %s %s".formatted(kind, mapping);
    }

    @Override
    public DateSchema with(final Constraint constraint) {
        return new DateSchema(kind, mapping, ConstrainedSchema.append(constraints, constraint));
    }
}
