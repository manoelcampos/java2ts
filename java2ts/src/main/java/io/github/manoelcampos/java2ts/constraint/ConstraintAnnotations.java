package io.github.manoelcampos.java2ts.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Reads Bean Validation constraint annotations and the constraints they are composed of.
 * Annotations are read by name, so there is no compile-time dependency on Bean Validation.
 * @author Manoel Campos
 */
public final class ConstraintAnnotations {
    private static final Set<String> CONSTRAINT_ANNOTATIONS = Set.of("jakarta.validation.Constraint", "javax.validation.Constraint");
    private static final String VALUE = "value";

    private ConstraintAnnotations() {/**/}

    /**
     * {@return true if an annotation is a Bean Validation constraint (annotated with {@code @Constraint}), false otherwise}
     * @param annotation the annotation to check
     */
    public static boolean isConstraint(final Annotation annotation) {
        return constraintAnnotation(annotation.annotationType()).isPresent();
    }

    /**
     * {@return the {@code @Constraint} annotation placed on an annotation type, or an empty Optional if it isn't a constraint}
     * @param type the annotation type
     */
    public static Optional<Annotation> constraintAnnotation(final Class<? extends Annotation> type) {
        return Arrays.stream(type.getAnnotations())
                     .filter(annotation -> CONSTRAINT_ANNOTATIONS.contains(annotation.annotationType().getName()))
                     .findFirst();
    }

    /**
     * {@return the constraints a constraint annotation type is composed of}, which are the constraint annotations placed on it,
     * including the ones inside containers of repeated constraints (such as {@code @Pattern.List}).
     * Other annotations, such as {@code @Target} and {@code @Constraint}, are ignored.
     * @param type the constraint annotation type
     */
    public static List<ComposingConstraint> composingConstraints(final Class<? extends Annotation> type) {
        return Arrays.stream(type.getAnnotations()).flatMap(ConstraintAnnotations::composingConstraints).toList();
    }

    /**
     * {@return the composing constraints of an annotation placed on a constraint annotation type}: the annotation itself if it's a constraint,
     * the constraints inside it (with their indexes) if it's a container of repeated constraints, or none otherwise
     * @param annotation the annotation placed on the annotation type
     */
    private static Stream<ComposingConstraint> composingConstraints(final Annotation annotation) {
        if (isConstraint(annotation))
            return Stream.of(new ComposingConstraint(annotation, ComposingConstraint.NO_INDEX));

        final List<Annotation> nested = nestedAnnotations(annotation);
        return IntStream.range(0, nested.size())
                        .filter(index -> isConstraint(nested.get(index)))
                        .mapToObj(index -> new ComposingConstraint(nested.get(index), index));
    }

    /**
     * {@return true if an annotation is a constraint composed (directly or inside other composed constraints)
     * of a constraint with one of the given names, false otherwise}
     * A constraint composed of itself is checked only once, so cycles don't loop forever.
     * @param annotation the annotation to check
     * @param names the fully qualified names of the constraints to look for
     */
    public static boolean isComposedOfAny(final Annotation annotation, final Set<String> names) {
        return isConstraint(annotation) && isComposedOfAny(annotation.annotationType(), names, new HashSet<>());
    }

    private static boolean isComposedOfAny(final Class<? extends Annotation> type, final Set<String> names, final Set<Class<?>> visited) {
        if (!visited.add(type))
            return false;

        return composingConstraints(type).stream()
                                         .map(constraint -> constraint.annotation().annotationType())
                                         .anyMatch(composing -> names.contains(composing.getName()) || isComposedOfAny(composing, names, visited));
    }

    /**
     * {@return the annotations of the {@code value} attribute of an annotation, when it is an annotation array},
     * such as the ones inside a container of repeated annotations ({@code @Pattern.List})
     * @param annotation the annotation to read
     * @throws IllegalStateException if the attribute can't be read
     */
    public static List<Annotation> nestedAnnotations(final Annotation annotation) {
        final Optional<Method> valueMethod = Arrays.stream(annotation.annotationType().getDeclaredMethods())
                                                   .filter(method -> method.getName().equals(VALUE))
                                                   .findFirst();
        return valueMethod.map(method -> invoke(annotation, method))
                          .filter(Annotation[].class::isInstance)
                          .map(value -> List.of((Annotation[]) value))
                          .orElse(List.of());
    }

    private static Object invoke(final Annotation annotation, final Method method) {
        try {
            method.setAccessible(true);
            return method.invoke(annotation);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            final var msg = "Error reading attribute %s of @%s".formatted(method.getName(), annotation.annotationType().getSimpleName());
            throw new IllegalStateException(msg, e);
        }
    }
}
