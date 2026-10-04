package io.github.manoelcampos.java2ts.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A property of a Java class or record, as seen by JSON serializers.
 *
 * @param name the property name
 * @param type the property type
 * @param elements the elements that define the property (such as a field, a getter and a record component),
 *                 in the order they must be checked for documentation and annotations
 * @author Manoel Campos
 */
public record JavaProperty(String name, AnnotatedType type, List<AnnotatedElement> elements) {
    /**
     * Creates a {@link JavaProperty}, validating the components and making immutable copies of collections.
     */
    public JavaProperty {
        requireNonNull(name);
        requireNonNull(type);
        elements = List.copyOf(elements);
    }

    /**
     * {@return a copy of this property with a different name}
     * @param newName the new name
     */
    public JavaProperty withName(final String newName) {
        return new JavaProperty(newName, type, elements);
    }

    /**
     * {@return true if the property can't be changed, false otherwise}
     * A property is read-only when it's a record component or its backing field is final.
     */
    public boolean isReadonly() {
        return elements.stream().anyMatch(element -> switch (element) {
            case RecordComponent component -> true;
            case Field field -> Modifier.isFinal(field.getModifiers());
            default -> false;
        });
    }

    /**
     * {@return all annotations of the property elements and its type}
     */
    public Stream<Annotation> annotations() {
        final Stream<Annotation> elementAnnotations = elements.stream().flatMap(element -> Arrays.stream(element.getAnnotations()));
        return Stream.concat(elementAnnotations, Arrays.stream(type.getAnnotations()));
    }

    /**
     * {@return the fully qualified names of all annotations of the property elements and its type},
     * including the ones with CLASS retention (such as {@code lombok.NonNull}), which aren't available via reflection.
     */
    public Stream<String> annotationNames() {
        final Stream<String> runtimeAnnotations = annotations().map(annotation -> annotation.annotationType().getName());
        final Stream<String> classAnnotations = elements.stream().flatMap(element -> ClassFileAnnotations.of(element).stream());
        return Stream.concat(runtimeAnnotations, classAnnotations);
    }

    /**
     * {@return true if the property has any of the given annotations (with RUNTIME or CLASS retention), false otherwise}
     * @param annotationNames fully qualified names of the annotations
     */
    public boolean hasAnyAnnotation(final Set<String> annotationNames) {
        return annotationNames().anyMatch(annotationNames::contains);
    }
}
