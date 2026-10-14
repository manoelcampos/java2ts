package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.constraint.ComposingConstraint;
import io.github.manoelcampos.java2ts.constraint.ConstraintAnnotations;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The definition of a Bean Validation constraint annotation type, read from the annotations placed on it
 * (by name, so there is no compile-time dependency on Bean Validation).
 * It tells if the constraint is composed only of other constraints, which can be converted to a schema.
 * @param type the constraint annotation type
 * @author Manoel Campos
 */
record ConstraintDefinition(Class<? extends Annotation> type) {
    private static final Set<String> REPORT_AS_SINGLE_VIOLATION =
        Set.of("jakarta.validation.ReportAsSingleViolation", "javax.validation.ReportAsSingleViolation");
    private static final String VALIDATED_BY = "validatedBy";

    /**
     * {@return the constraints this constraint is composed of}
     * @see ConstraintAnnotations#composingConstraints(Class)
     */
    List<ComposingConstraint> composingConstraints() {
        return ConstraintAnnotations.composingConstraints(type);
    }

    /**
     * {@return true if the composing constraints report a single violation with the message of this constraint
     * (it's annotated with {@code @ReportAsSingleViolation}), false otherwise}
     */
    boolean reportsAsSingleViolation() {
        return metaAnnotation(type, REPORT_AS_SINGLE_VIOLATION).isPresent();
    }

    /**
     * Checks if the constraint can be converted to a schema from its composing constraints.
     * @throws UnsupportedValidationException if the constraint has a validator (whose Java code can't be converted)
     *         or has no composing constraints
     */
    void checkComposable() {
        if (hasValidators())
            throw ConstraintReader.unsupported(type.getSimpleName(), "its validator can't be converted to a schema");

        if (composingConstraints().isEmpty())
            throw ConstraintReader.unsupported(type.getSimpleName());
    }

    /**
     * {@return true if the {@code validatedBy} attribute of {@code @Constraint} has any validator class, false otherwise}
     */
    boolean hasValidators() {
        return ConstraintAnnotations.constraintAnnotation(type)
                   .map(constraint -> ((Object[]) new AnnotationAttributes(constraint).value(VALIDATED_BY)).length > 0)
                   .orElse(false);
    }

    /**
     * {@return the first annotation of an element whose type has one of the given names, or an empty Optional if there is none}
     * @param element the annotated element (such as an annotation type or one of its attributes)
     * @param names the fully qualified names of the annotation types to look for
     */
    static Optional<Annotation> metaAnnotation(final AnnotatedElement element, final Set<String> names) {
        return Arrays.stream(element.getAnnotations())
                     .filter(annotation -> names.contains(annotation.annotationType().getName()))
                     .findFirst();
    }
}
