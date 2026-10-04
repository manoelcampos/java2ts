package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.Set;

/**
 * A validation constraint, built from a Bean Validation annotation, independent of any validation library.
 * @author Manoel Campos
 */
public sealed interface Constraint
    permits LengthConstraint, NotBlankConstraint, PatternConstraint, EmailConstraint, UrlConstraint,
            NumericBoundConstraint, DigitsConstraint, BooleanConstraint, TemporalConstraint
{
    /**
     * {@return the error message defined by the user, or an empty Optional to use the library default message}
     */
    Optional<String> message();

    /**
     * {@return the kinds of values this constraint can be applied to}
     */
    Set<ConstraintTarget> targets();
}
