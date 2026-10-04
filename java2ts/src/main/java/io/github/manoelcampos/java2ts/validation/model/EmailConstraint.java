package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a string to be an e-mail address.
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record EmailConstraint(Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEXT);
    }
}
