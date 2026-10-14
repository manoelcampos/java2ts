package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.constraint.ComposingConstraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The attributes of a composed constraint that override attributes of its composing constraints
 * (annotated with Bean Validation's {@code @OverridesAttribute} or {@code @OverridesAttribute.List}).
 * They're read by name, so there is no compile-time dependency on Bean Validation.
 * @param overrides the attribute overrides
 * @author Manoel Campos
 */
record AttributeOverrides(List<AttributeOverride> overrides) {
    private static final Set<String> OVERRIDES_ATTRIBUTE = Set.of("jakarta.validation.OverridesAttribute", "javax.validation.OverridesAttribute");
    private static final Set<String> OVERRIDES_ATTRIBUTE_LIST =
        Set.of("jakarta.validation.OverridesAttribute$List", "javax.validation.OverridesAttribute$List");
    private static final String CONSTRAINT = "constraint";
    private static final String NAME = "name";
    private static final String CONSTRAINT_INDEX = "constraintIndex";

    AttributeOverrides {
        overrides = List.copyOf(overrides);
    }

    /**
     * {@return the attribute overrides of a composed constraint}
     * @param composed the attributes of the composed constraint, whose values (which may be overridden too) replace the composing ones
     * @param composing the composing constraints, which must be the target of every override
     * @throws io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException
     *         if an override doesn't target an attribute of one of the composing constraints
     */
    static AttributeOverrides of(final AnnotationAttributes composed, final List<ComposingConstraint> composing) {
        final List<AttributeOverride> overrides = Arrays.stream(composed.annotation().annotationType().getDeclaredMethods())
                                                        .sorted(Comparator.comparing(Method::getName))
                                                        .flatMap(method -> overridesOf(composed, method))
                                                        .toList();
        overrides.forEach(override -> checkTarget(composed, override, composing));
        return new AttributeOverrides(overrides);
    }

    /**
     * {@return the overrides defined by an attribute of a composed constraint}
     * @param composed the attributes of the composed constraint
     * @param method the attribute, which may have one {@code @OverridesAttribute} or many (inside {@code @OverridesAttribute.List})
     */
    private static Stream<AttributeOverride> overridesOf(final AnnotationAttributes composed, final Method method) {
        final Stream<Annotation> single = ConstraintDefinition.metaAnnotation(method, OVERRIDES_ATTRIBUTE).stream();
        final Stream<Annotation> listed = ConstraintDefinition.metaAnnotation(method, OVERRIDES_ATTRIBUTE_LIST).stream()
                                                              .flatMap(list -> new AnnotationAttributes(list).nestedAnnotations().stream());
        return Stream.concat(single, listed).map(annotation -> override(composed, method, new AnnotationAttributes(annotation)));
    }

    /**
     * {@return an attribute override}
     * @param composed the attributes of the composed constraint, which define the overriding value
     * @param method the composed constraint attribute that overrides another one
     * @param overridesAttribute the attributes of the {@code @OverridesAttribute} annotation;
     *        when its {@code name} is empty, the overridden attribute has the same name as the method
     */
    private static AttributeOverride override(final AnnotationAttributes composed, final Method method, final AnnotationAttributes overridesAttribute) {
        final String name = overridesAttribute.string(NAME);
        final String overriddenName = name.isEmpty() ? method.getName() : name;
        final var constraint = (Class<?>) overridesAttribute.value(CONSTRAINT);
        final int index = overridesAttribute.intValue(CONSTRAINT_INDEX);
        return new AttributeOverride(method.getName(), constraint, index, overriddenName, composed.value(method.getName()));
    }

    /**
     * Checks if an override targets an attribute of one of the composing constraints.
     * @throws io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException if it doesn't
     */
    private static void checkTarget(final AnnotationAttributes composed, final AttributeOverride override, final List<ComposingConstraint> composing) {
        final boolean found = composing.stream()
                                       .filter(override::targets)
                                       .anyMatch(constraint -> new AnnotationAttributes(constraint.annotation()).has(override.name()));
        if (!found) {
            final var reason = "@OverridesAttribute on attribute %s doesn't match the attribute %s of a composing @%s with constraintIndex %d"
                .formatted(override.sourceAttribute(), override.name(), override.constraint().getSimpleName(), override.constraintIndex());
            throw ConstraintReader.unsupported(composed.simpleName(), reason);
        }
    }

    /**
     * {@return the values that replace attributes of a composing constraint, indexed by the attribute name}
     * @param constraint the composing constraint
     */
    Map<String, Object> valuesFor(final ComposingConstraint constraint) {
        return overrides.stream()
                        .filter(override -> override.targets(constraint))
                        .collect(Collectors.toUnmodifiableMap(AttributeOverride::name, AttributeOverride::value));
    }
}
