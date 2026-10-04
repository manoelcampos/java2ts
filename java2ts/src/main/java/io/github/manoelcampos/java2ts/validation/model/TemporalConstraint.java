package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a date to be in the past or in the future, compared with the current date/time.
 * @param past true if the date must be in the past, false if it must be in the future
 * @param orPresent true if the current date/time is valid too
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record TemporalConstraint(boolean past, boolean orPresent, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEMPORAL);
    }
}
