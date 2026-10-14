package io.github.manoelcampos.java2ts.validation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The composed constraints being read, from the outermost to the innermost one,
 * used to find cycles and the annotation whose message the composing constraints must use.
 * @param types the types of the composed constraints being read
 * @param messageSource the attributes of the outermost composed constraint with {@code @ReportAsSingleViolation},
 *        whose message replaces the messages of all constraints inside it, or an empty Optional if there is none
 * @author Manoel Campos
 */
record CompositionPath(List<Class<? extends Annotation>> types, Optional<AnnotationAttributes> messageSource) {
    /** The path of a constraint placed directly on a property (not inside a composed constraint). */
    static final CompositionPath ROOT = new CompositionPath(List.of(), Optional.empty());

    private static final String SEPARATOR = " -> ";

    CompositionPath {
        types = List.copyOf(types);
    }

    /**
     * {@return a new path to read the composing constraints of a composed constraint}
     * @param composed the attributes of the composed constraint whose composing constraints will be read
     * @param reportsAsSingleViolation true if the composed constraint has {@code @ReportAsSingleViolation},
     *        so its message is used by the composing constraints (unless an outer constraint already defines the message)
     * @throws io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException
     *         if the composed constraint is already in the path (it's composed of itself)
     */
    CompositionPath enter(final AnnotationAttributes composed, final boolean reportsAsSingleViolation) {
        final Class<? extends Annotation> type = composed.annotation().annotationType();
        final var newTypes = Stream.concat(types.stream(), Stream.of(type)).toList();
        if (types.contains(type))
            throw ConstraintReader.unsupported(type.getSimpleName(), "it's composed of itself (%s)".formatted(format(newTypes)));

        final Optional<AnnotationAttributes> newMessageSource = reportsAsSingleViolation ? messageSource.or(() -> Optional.of(composed)) : messageSource;
        return new CompositionPath(newTypes, newMessageSource);
    }

    /**
     * {@return the attributes of a constraint in this path}, using the message of the {@link #messageSource()} when there is one
     * @param annotation the constraint to read
     * @param overrides values that replace the ones of some attributes of the constraint, indexed by the attribute name
     */
    AnnotationAttributes attributesOf(final Annotation annotation, final Map<String, Object> overrides) {
        return new AnnotationAttributes(annotation, overrides, messageSource);
    }

    private static String format(final List<Class<? extends Annotation>> types) {
        return types.stream().map(type -> "@" + type.getSimpleName()).collect(Collectors.joining(SEPARATOR));
    }
}
