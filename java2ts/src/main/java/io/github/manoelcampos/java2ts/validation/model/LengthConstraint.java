package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Limits the length of a string or the size of an array.
 * @param min the minimum length, if any
 * @param max the maximum length, if any
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record LengthConstraint(OptionalInt min, OptionalInt max, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEXT, ConstraintTarget.ARRAY);
    }
}
