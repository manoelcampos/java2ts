package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.validation.model.BooleanConstraint;
import io.github.manoelcampos.java2ts.validation.model.Constraint;
import io.github.manoelcampos.java2ts.validation.model.DigitsConstraint;
import io.github.manoelcampos.java2ts.validation.model.EmailConstraint;
import io.github.manoelcampos.java2ts.validation.model.LengthConstraint;
import io.github.manoelcampos.java2ts.validation.model.NotBlankConstraint;
import io.github.manoelcampos.java2ts.validation.model.NumericBoundConstraint;
import io.github.manoelcampos.java2ts.validation.model.PatternConstraint;
import io.github.manoelcampos.java2ts.validation.model.TemporalConstraint;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;
import io.github.manoelcampos.java2ts.validation.model.UrlConstraint;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Converts Bean Validation annotations (from the {@code jakarta.validation.constraints} and
 * {@code javax.validation.constraints} packages, plus some Hibernate Validator ones) into {@link Constraint}s.
 * Annotations are read by name, so there is no compile-time dependency on them.
 *
 * <p>A constraint annotation (one annotated with {@code @Constraint}) that isn't supported
 * fails the conversion, so that backend and frontend validations can't drift.</p>
 * @author Manoel Campos
 */
public final class ConstraintReader {
    private static final List<String> STANDARD_PACKAGES = List.of("jakarta.validation.constraints.", "javax.validation.constraints.");
    private static final String HIBERNATE_PACKAGE = "org.hibernate.validator.constraints.";
    private static final Set<String> CONSTRAINT_ANNOTATIONS = Set.of("jakarta.validation.Constraint", "javax.validation.Constraint");

    /** Constraints that don't change the schema, since they only make a property required. */
    private static final String NOT_NULL = "NotNull";

    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String VALUE = "value";
    private static final String INCLUSIVE = "inclusive";
    private static final String REGEXP = "regexp";
    private static final String FLAGS = "flags";
    private static final String ANY_REGEXP = ".*";

    private final Map<String, Function<AnnotationAttributes, List<Constraint>>> factories = new HashMap<>();

    /**
     * Creates a constraint reader.
     */
    public ConstraintReader() {
        STANDARD_PACKAGES.forEach(this::registerStandard);
        register(HIBERNATE_PACKAGE + "Length", ConstraintReader::length);
        register(HIBERNATE_PACKAGE + "Range", ConstraintReader::range);
        register(HIBERNATE_PACKAGE + "URL", ConstraintReader::url);
    }

    private void registerStandard(final String pkg) {
        register(pkg + NOT_NULL, attrs -> List.of());
        register(pkg + "NotBlank", attrs -> List.of(new NotBlankConstraint(attrs.message())));
        register(pkg + "NotEmpty", attrs -> List.of(new LengthConstraint(OptionalInt.of(1), OptionalInt.empty(), attrs.message())));
        register(pkg + "Size", ConstraintReader::length);
        register(pkg + "Min", attrs -> bound(attrs, true, true));
        register(pkg + "Max", attrs -> bound(attrs, false, true));
        register(pkg + "DecimalMin", attrs -> bound(attrs, true, attrs.bool(INCLUSIVE)));
        register(pkg + "DecimalMax", attrs -> bound(attrs, false, attrs.bool(INCLUSIVE)));
        register(pkg + "Positive", attrs -> zeroBound(attrs, true, false));
        register(pkg + "PositiveOrZero", attrs -> zeroBound(attrs, true, true));
        register(pkg + "Negative", attrs -> zeroBound(attrs, false, false));
        register(pkg + "NegativeOrZero", attrs -> zeroBound(attrs, false, true));
        register(pkg + "Email", ConstraintReader::email);
        register(pkg + "Pattern", ConstraintReader::pattern);
        register(pkg + "Digits", attrs -> List.of(new DigitsConstraint(attrs.intValue("integer"), attrs.intValue("fraction"), attrs.message())));
        register(pkg + "AssertTrue", attrs -> List.of(new BooleanConstraint(true, attrs.message())));
        register(pkg + "AssertFalse", attrs -> List.of(new BooleanConstraint(false, attrs.message())));
        register(pkg + "Past", attrs -> List.of(new TemporalConstraint(true, false, attrs.message())));
        register(pkg + "PastOrPresent", attrs -> List.of(new TemporalConstraint(true, true, attrs.message())));
        register(pkg + "Future", attrs -> List.of(new TemporalConstraint(false, false, attrs.message())));
        register(pkg + "FutureOrPresent", attrs -> List.of(new TemporalConstraint(false, true, attrs.message())));
    }

    private void register(final String annotationName, final Function<AnnotationAttributes, List<Constraint>> factory) {
        factories.put(annotationName, factory);
    }

    /**
     * Reads the constraints defined by an annotation.
     * @param annotation the annotation to read
     * @return the constraints, or an empty list if the annotation isn't a constraint
     *         (or is a constraint that doesn't change the schema, such as {@code @NotNull})
     * @throws UnsupportedValidationException if the annotation is a constraint that isn't supported
     */
    public List<Constraint> read(final Annotation annotation) {
        final var attributes = new AnnotationAttributes(annotation);
        final Function<AnnotationAttributes, List<Constraint>> factory = factories.get(annotation.annotationType().getName());
        if (factory != null)
            return factory.apply(attributes);

        if (isConstraint(annotation))
            throw unsupported(attributes.simpleName());

        return attributes.nestedAnnotations().stream().flatMap(nested -> read(nested).stream()).toList();
    }

    /**
     * {@return true if an annotation is a Bean Validation constraint (annotated with {@code @Constraint}), false otherwise}
     * @param annotation the annotation to check
     */
    public static boolean isConstraint(final Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getAnnotations())
                     .anyMatch(meta -> CONSTRAINT_ANNOTATIONS.contains(meta.annotationType().getName()));
    }

    /**
     * {@return an exception indicating a constraint annotation isn't supported}
     * @param simpleName the simple name of the annotation
     */
    static UnsupportedValidationException unsupported(final String simpleName) {
        return new UnsupportedValidationException(
            "constraint @%s isn't supported. Exclude the class from validation and write its schema by hand".formatted(simpleName));
    }

    private static List<Constraint> length(final AnnotationAttributes attrs) {
        final int min = attrs.intValue(MIN);
        final int max = attrs.intValue(MAX);
        final OptionalInt optionalMin = min > 0 ? OptionalInt.of(min) : OptionalInt.empty();
        final OptionalInt optionalMax = max < Integer.MAX_VALUE ? OptionalInt.of(max) : OptionalInt.empty();
        return List.of(new LengthConstraint(optionalMin, optionalMax, attrs.message()));
    }

    private static List<Constraint> bound(final AnnotationAttributes attrs, final boolean lower, final boolean inclusive) {
        return List.of(new NumericBoundConstraint(attrs.decimal(VALUE), lower, inclusive, attrs.message()));
    }

    private static List<Constraint> zeroBound(final AnnotationAttributes attrs, final boolean lower, final boolean inclusive) {
        return List.of(new NumericBoundConstraint(BigDecimal.ZERO, lower, inclusive, attrs.message()));
    }

    /**
     * {@return the constraints of Hibernate's {@code @Range}}, whose default maximum value means there is no maximum
     */
    private static List<Constraint> range(final AnnotationAttributes attrs) {
        final var min = new NumericBoundConstraint(attrs.decimal(MIN), true, true, attrs.message());
        final BigDecimal max = attrs.decimal(MAX);
        final Optional<Constraint> maxConstraint = max.equals(BigDecimal.valueOf(Long.MAX_VALUE)) ?
                Optional.empty() :
                Optional.of(new NumericBoundConstraint(max, false, true, attrs.message()));
        return Stream.concat(Stream.of(min), maxConstraint.stream()).toList();
    }

    /**
     * {@return the constraints of {@code @Email}}, including a pattern when the annotation defines one
     */
    private static List<Constraint> email(final AnnotationAttributes attrs) {
        final var email = new EmailConstraint(attrs.message());
        final String regexp = attrs.string(REGEXP);
        if (regexp.equals(ANY_REGEXP))
            return List.of(email);

        return List.of(email, patternConstraint(attrs));
    }

    private static List<Constraint> pattern(final AnnotationAttributes attrs) {
        return List.of(patternConstraint(attrs));
    }

    /**
     * {@return a pattern constraint}, checking if its regular expression can be converted to JavaScript
     */
    private static PatternConstraint patternConstraint(final AnnotationAttributes attrs) {
        final String regexp = attrs.string(REGEXP);
        final Set<String> flags = Set.copyOf(attrs.enumNames(FLAGS));
        JsRegex.check(regexp, flags);
        return new PatternConstraint(regexp, flags, attrs.message());
    }

    /**
     * {@return the constraints of Hibernate's {@code @URL}}, which are supported only when no URL part is restricted
     */
    private static List<Constraint> url(final AnnotationAttributes attrs) {
        final boolean restricted = !attrs.string("protocol").isEmpty() || !attrs.string("host").isEmpty() ||
                                   attrs.intValue("port") != -1 || !attrs.string(REGEXP).equals(ANY_REGEXP);
        if (restricted)
            throw new UnsupportedValidationException(
                "@URL with protocol, host, port or regexp isn't supported. Exclude the class from validation and write its schema by hand");

        return List.of(new UrlConstraint(attrs.message()));
    }
}
