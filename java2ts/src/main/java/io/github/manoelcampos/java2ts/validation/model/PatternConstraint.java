package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * Requires a whole string to match a Java regular expression.
 * @param regex the Java regular expression
 * @param flags the names of the {@code jakarta.validation.constraints.Pattern.Flag}s
 * @param message the error message defined by the user, if any
 * @author Manoel Campos
 */
public record PatternConstraint(String regex, Set<String> flags, Optional<String> message) implements Constraint {
    @Override
    public Set<ConstraintTarget> targets() {
        return Set.of(ConstraintTarget.TEXT);
    }
}
