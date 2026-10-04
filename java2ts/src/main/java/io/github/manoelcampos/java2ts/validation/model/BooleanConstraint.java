package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a boolean to have a specific value.
 * @param value the only valid value
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record BooleanConstraint(boolean value, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.BOOLEAN);
    }
}
