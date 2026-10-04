package io.github.manoelcampos.java2ts.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Handles the Jackson annotations that change which properties are serialized and their names:
 * {@code @JsonIgnore} and {@code @JsonProperty}.
 * Annotations are identified by their simple names, so that the library doesn't depend on Jackson
 * and works with both Jackson 2 and 3.
 * @author Manoel Campos
 */
public final class JacksonAnnotations {
    private static final String JSON_IGNORE = "JsonIgnore";
    private static final String JSON_PROPERTY = "JsonProperty";

    private JacksonAnnotations() {/**/}

    /**
     * Removes the ignored properties and renames the ones annotated with {@code @JsonProperty("newName")}.
     * @param properties the properties to process
     * @return the processed properties
     */
    public static List<JavaProperty> apply(final List<JavaProperty> properties) {
        return properties.stream()
                         .filter(property -> !isIgnored(property))
                         .map(property -> renamedName(property.elements()).map(property::withName).orElse(property))
                         .toList();
    }

    /**
     * {@return true if any element of a property has a {@code @JsonIgnore} annotation whose value is true}
     * @param property the property to check
     */
    public static boolean isIgnored(final JavaProperty property) {
        return property.elements().stream()
                       .flatMap(element -> find(element, JSON_IGNORE).stream())
                       .anyMatch(annotation -> value(annotation, Boolean.class).orElse(true));
    }

    /**
     * {@return true if an element has the {@code @JsonProperty} annotation, false otherwise}
     * @param element the element to check
     */
    public static boolean hasJsonProperty(final AnnotatedElement element) {
        return find(element, JSON_PROPERTY).isPresent();
    }

    /**
     * {@return the name defined by a {@code @JsonProperty("name")} annotation in any of the elements,
     * or an empty Optional if there is no such annotation or it has no name}
     * @param elements the elements to check
     */
    public static Optional<String> renamedName(final List<? extends AnnotatedElement> elements) {
        return elements.stream()
                       .flatMap(element -> find(element, JSON_PROPERTY).stream())
                       .flatMap(annotation -> value(annotation, String.class).stream())
                       .filter(name -> !name.isBlank())
                       .findFirst();
    }

    private static Optional<Annotation> find(final AnnotatedElement element, final String simpleName) {
        return Arrays.stream(element.getAnnotations())
                     .filter(annotation -> annotation.annotationType().getSimpleName().equals(simpleName))
                     .findFirst();
    }

    private static <T> Optional<T> value(final Annotation annotation, final Class<T> valueClass) {
        try {
            final Object value = annotation.annotationType().getMethod("value").invoke(annotation);
            return valueClass.isInstance(value) ? Optional.of(valueClass.cast(value)) : Optional.empty();
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return Optional.empty();
        }
    }
}
