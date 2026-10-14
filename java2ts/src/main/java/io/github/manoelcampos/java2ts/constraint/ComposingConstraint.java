package io.github.manoelcampos.java2ts.constraint;

import java.lang.annotation.Annotation;

/**
 * A constraint placed on a composed constraint annotation type.
 * @param annotation the composing constraint
 * @param index the index of the constraint inside a container of repeated constraints (such as {@code @Pattern.List}),
 *        or {@link #NO_INDEX} if it's placed directly on the annotation type.
 *        It's the index used by the {@code constraintIndex} of {@code @OverridesAttribute}.
 * @author Manoel Campos
 */
public record ComposingConstraint(Annotation annotation, int index) {
    /** The index of a constraint that isn't inside a container, which is also the default {@code constraintIndex} of {@code @OverridesAttribute}. */
    public static final int NO_INDEX = -1;
}
