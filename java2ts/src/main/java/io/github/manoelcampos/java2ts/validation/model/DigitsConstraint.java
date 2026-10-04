package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Limits the number of integer and fraction digits of a number.
 * @param integer the maximum number of integer digits
 * @param fraction the maximum number of fraction digits
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record DigitsConstraint(int integer, int fraction, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.NUMBER);
    }
}
