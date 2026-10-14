package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.constraint.ComposingConstraint;

/**
 * An attribute of a composed constraint that overrides an attribute of one of its composing constraints
 * (defined by Bean Validation's {@code @OverridesAttribute}).
 * @param sourceAttribute the name of the composed constraint attribute that defines the value
 * @param constraint the type of the composing constraint whose attribute is overridden
 * @param constraintIndex the index of the composing constraint inside a container of repeated constraints,
 *        or {@link ComposingConstraint#NO_INDEX} for a constraint placed directly on the composed annotation type
 * @param name the name of the overridden attribute
 * @param value the value that replaces the one of the overridden attribute
 * @author Manoel Campos
 */
record AttributeOverride(String sourceAttribute, Class<?> constraint, int constraintIndex, String name, Object value) {
    /**
     * {@return true if this override targets a composing constraint, false otherwise}
     * @param composing the composing constraint to check
     */
    boolean targets(final ComposingConstraint composing) {
        return composing.annotation().annotationType().equals(constraint) && composing.index() == constraintIndex;
    }
}
