package io.github.manoelcampos.java2ts.validation.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * Limits the minimum or maximum value of a number.
 * @param value the limit
 * @param lower true if the limit is the minimum value, false if it's the maximum value
 * @param inclusive true if the limit itself is valid, false otherwise
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record NumericBoundConstraint(BigDecimal value, boolean lower, boolean inclusive, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.NUMBER);
    }
}
