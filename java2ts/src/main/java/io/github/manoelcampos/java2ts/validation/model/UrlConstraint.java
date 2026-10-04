package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a string to be a URL.
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record UrlConstraint(Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEXT);
    }
}
