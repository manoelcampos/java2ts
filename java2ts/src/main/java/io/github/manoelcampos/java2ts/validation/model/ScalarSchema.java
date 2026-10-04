package io.github.manoelcampos.java2ts.validation.model;

import io.github.manoelcampos.java2ts.parser.type.BasicKind;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The schema of a value serialized as a JSON primitive value, such as a string or number.
 * @param kind the kind of the value
 * @param constraints the constraints of the value
 * @author Manoel Campos
 */
public record ScalarSchema(BasicKind kind, List<Constraint> constraints) implements ConstrainedSchema {
    /**
     * Creates a {@link ScalarSchema}, validating the components and making immutable copies of collections.
     */
    public ScalarSchema {
        requireNonNull(kind);
        constraints = List.copyOf(constraints);
    }

    /**
     * Creates a {@link ScalarSchema} without constraints.
     * @param kind the kind of the value
     */
    public ScalarSchema(final BasicKind kind) {
        this(kind, List.of());
    }

    @Override
    public Optional<ConstraintTarget> constraintTarget() {
        return switch (kind) {
            case STRING, URI -> Optional.of(ConstraintTarget.TEXT);
            case INTEGER, DECIMAL -> Optional.of(ConstraintTarget.NUMBER);
            case BOOLEAN -> Optional.of(ConstraintTarget.BOOLEAN);
            default -> Optional.empty();
        };
    }

    @Override
    public String describe() {
        return kind.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ScalarSchema with(final Constraint constraint) {
        return new ScalarSchema(kind, ConstrainedSchema.append(constraints, constraint));
    }
}
