package io.github.manoelcampos.java2ts.validation.model;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The schema of an array or collection.
 * @param element the schema of the elements
 * @param constraints the constraints of the array (such as its size)
 * @author Manoel Campos
 */
public record ArraySchema(SchemaType element, List<Constraint> constraints) implements ConstrainedSchema {
    /**
     * Creates an {@link ArraySchema}, validating the components and making immutable copies of collections.
     */
    public ArraySchema {
        requireNonNull(element);
        constraints = List.copyOf(constraints);
    }

    /**
     * Creates an {@link ArraySchema} without constraints.
     * @param element the schema of the elements
     */
    public ArraySchema(final SchemaType element) {
        this(element, List.of());
    }

    @Override
    public Optional<ConstraintTarget> constraintTarget() {
        return Optional.of(ConstraintTarget.ARRAY);
    }

    @Override
    public String describe() {
        return "array";
    }

    @Override
    public ArraySchema with(final Constraint constraint) {
        return new ArraySchema(element, ConstrainedSchema.append(constraints, constraint));
    }
}
