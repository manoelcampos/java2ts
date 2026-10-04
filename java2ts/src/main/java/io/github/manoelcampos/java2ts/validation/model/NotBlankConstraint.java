package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a string to have at least one non-whitespace character.
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record NotBlankConstraint(Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEXT);
    }
}
