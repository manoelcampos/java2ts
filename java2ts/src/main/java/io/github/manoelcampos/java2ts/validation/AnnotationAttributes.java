package io.github.manoelcampos.java2ts.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the attributes of an annotation by name, so that annotations (such as Bean Validation ones)
 * can be read without a compile-time dependency on them.
 * @param annotation the annotation to read
 * @author Manoel Campos
 */
record AnnotationAttributes(Annotation annotation) {
    private static final String MESSAGE = "message";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z0-9_]+)}");
    private static final String TEMPLATE_START = "{";
    private static final String EXPRESSION_START = "${";

    /**
     * {@return the simple name of the annotation type, such as {@code Size}}
     */
    String simpleName() {
        return annotation.annotationType().getSimpleName();
    }

    /**
     * {@return true if the annotation has an attribute, false otherwise}
     * @param name the attribute name
     */
    boolean has(final String name) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods()).anyMatch(method -> method.getName().equals(name));
    }

    /**
     * {@return the value of an attribute}
     * @param name the attribute name
     * @throws IllegalStateException if the attribute can't be read
     */
    Object value(final String name) {
        try {
            final Method method = annotation.annotationType().getMethod(name);
            method.setAccessible(true);
            return method.invoke(annotation);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error reading attribute %s of @%s".formatted(name, simpleName()), e);
        }
    }

    /**
     * {@return the value of an int attribute}
     * @param name the attribute name
     */
    int intValue(final String name) {
        return ((Number) value(name)).intValue();
    }

    /**
     * {@return the value of a numeric attribute (such as a long or a string with a decimal number)}
     * @param name the attribute name
     */
    BigDecimal decimal(final String name) {
        return new BigDecimal(value(name).toString());
    }

    /**
     * {@return the value of a string attribute}
     * @param name the attribute name
     */
    String string(final String name) {
        return value(name).toString();
    }

    /**
     * {@return the value of a boolean attribute}
     * @param name the attribute name
     */
    boolean bool(final String name) {
        return (Boolean) value(name);
    }

    /**
     * {@return the names of the enum constants of an enum array attribute}
     * @param name the attribute name
     */
    List<String> enumNames(final String name) {
        return Arrays.stream((Enum<?>[]) value(name)).map(Enum::name).toList();
    }

    /**
     * {@return the annotations of the {@code value} attribute, when it is an annotation array},
     * such as the ones inside a container of repeated annotations ({@code @Pattern.List})
     */
    List<Annotation> nestedAnnotations() {
        if (!has("value"))
            return List.of();

        return value("value") instanceof Annotation[] annotations ? List.of(annotations) : List.of();
    }

    /**
     * {@return the error message defined by the user, or an empty Optional if there is none}
     * Messages that are templates (such as {@code {jakarta.validation.constraints.Size.message}})
     * are ignored, since they're translated by the backend. Placeholders with attribute names
     * (such as {@code {min}}) are replaced by the attribute values. A message with an
     * expression (such as {@code ${validatedValue}}) or unknown placeholders is ignored too.
     */
    Optional<String> message() {
        if (!has(MESSAGE))
            return Optional.empty();

        final String message = replacePlaceholders(string(MESSAGE));
        return message.isBlank() || message.contains(TEMPLATE_START) || message.contains(EXPRESSION_START) ?
                Optional.empty() :
                Optional.of(message);
    }

    private String replacePlaceholders(final String message) {
        final Matcher matcher = PLACEHOLDER.matcher(message);
        return matcher.replaceAll(result -> {
            final String name = result.group(1);
            final String replacement = has(name) && !value(name).getClass().isArray() ? value(name).toString() : result.group();
            return Matcher.quoteReplacement(replacement);
        });
    }
}
