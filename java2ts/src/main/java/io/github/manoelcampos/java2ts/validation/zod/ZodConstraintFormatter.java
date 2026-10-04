package io.github.manoelcampos.java2ts.validation.zod;

import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.parser.type.DateKind;
import io.github.manoelcampos.java2ts.validation.JsRegex;
import io.github.manoelcampos.java2ts.validation.model.BooleanConstraint;
import io.github.manoelcampos.java2ts.validation.model.Constraint;
import io.github.manoelcampos.java2ts.validation.model.DateSchema;
import io.github.manoelcampos.java2ts.validation.model.DigitsConstraint;
import io.github.manoelcampos.java2ts.validation.model.EmailConstraint;
import io.github.manoelcampos.java2ts.validation.model.LengthConstraint;
import io.github.manoelcampos.java2ts.validation.model.NotBlankConstraint;
import io.github.manoelcampos.java2ts.validation.model.NumericBoundConstraint;
import io.github.manoelcampos.java2ts.validation.model.PatternConstraint;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;
import io.github.manoelcampos.java2ts.validation.model.TemporalConstraint;
import io.github.manoelcampos.java2ts.validation.model.UrlConstraint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * Writes {@link Constraint}s as calls to Zod 4 schema methods, such as {@code .min(1)} or {@code .regex(/\S/)}.
 * Constraints that change the base schema (such as {@link EmailConstraint}, which turns {@code z.string()}
 * into {@code z.email()}) are written by the {@link ZodTypeFormatter}.
 * @author Manoel Campos
 */
final class ZodConstraintFormatter {
    /** Name of the helper function that returns the current local date as an ISO-8601 string. */
    static final String LOCAL_DATE_FUNCTION = "localDate";

    private static final String NOT_BLANK_REGEX = "/\\S/";
    private final boolean defaultMessages;
    private boolean usesLocalDate;

    /**
     * Creates a constraint formatter.
     * @param defaultMessages if true, English messages are written for the constraints that Zod can't describe
     *                        (such as {@code @Past}, which is checked by a refinement). If false, Zod's
     *                        generic (possibly translated) message is used when the user doesn't define one
     */
    ZodConstraintFormatter(final boolean defaultMessages) {
        this.defaultMessages = defaultMessages;
    }

    /**
     * {@return true if any constraint written so far uses the {@link #LOCAL_DATE_FUNCTION} helper, false otherwise}
     */
    boolean usesLocalDate() {
        return usesLocalDate;
    }

    /**
     * {@return the method calls for a list of constraints}
     * @param constraints the constraints to write
     * @param schema the schema the constraints apply to
     */
    String format(final List<Constraint> constraints, final SchemaType schema) {
        return constraints.stream().map(constraint -> format(constraint, schema)).collect(Collectors.joining());
    }

    private String format(final Constraint constraint, final SchemaType schema) {
        return switch (constraint) {
            case LengthConstraint length -> length(length);
            case NotBlankConstraint notBlank -> call("regex", NOT_BLANK_REGEX, notBlank.message());
            case PatternConstraint pattern -> call("regex", JsRegex.of(pattern.regex(), pattern.flags()), pattern.message());
            case NumericBoundConstraint bound -> call(boundMethod(bound), number(bound.value()), bound.message());
            case DigitsConstraint digits -> digits(digits);
            case TemporalConstraint temporal -> temporal(temporal, (DateSchema) schema);
            case EmailConstraint email -> "";
            case UrlConstraint url -> "";
            case BooleanConstraint bool -> "";
        };
    }

    private static String length(final LengthConstraint length) {
        final OptionalInt min = length.min();
        final OptionalInt max = length.max();
        if (min.isPresent() && max.isPresent() && min.getAsInt() == max.getAsInt())
            return call("length", String.valueOf(min.getAsInt()), length.message());

        final String minCall = min.isPresent() ? call("min", String.valueOf(min.getAsInt()), length.message()) : "";
        final String maxCall = max.isPresent() ? call("max", String.valueOf(max.getAsInt()), length.message()) : "";
        return minCall + maxCall;
    }

    private static String boundMethod(final NumericBoundConstraint bound) {
        if (bound.lower())
            return bound.inclusive() ? "gte" : "gt";

        return bound.inclusive() ? "lte" : "lt";
    }

    private String digits(final DigitsConstraint digits) {
        final String fraction = digits.fraction() > 0 ? "(\\.\\d{0,%d})?".formatted(digits.fraction()) : "";
        final String regex = "/^\\d{0,%d}%s$/".formatted(digits.integer(), fraction);
        final String defaultMessage = "numeric value out of bounds (<%d digits>.<%d digits> expected)".formatted(digits.integer(), digits.fraction());
        return refine("v => %s.test(String(Math.abs(v)))".formatted(regex), message(digits.message(), defaultMessage));
    }

    /**
     * {@return a refinement comparing a date with the current date/time}
     * Dates represented as {@code Date} objects are compared by their time. Local dates (without time)
     * are compared as ISO-8601 strings with the current local date. Other date strings are parsed:
     * JavaScript parses date-times without offset (such as {@code LocalDateTime}) as local times.
     */
    private String temporal(final TemporalConstraint temporal, final DateSchema schema) {
        final String operator = (temporal.past() ? "<" : ">") + (temporal.orPresent() ? "=" : "");
        final String comparison;
        if (schema.mapping() == DateMapping.asDate)
            comparison = "v.getTime() %s Date.now()".formatted(operator);
        else if (schema.kind() == DateKind.LOCAL_DATE) {
            usesLocalDate = true;
            comparison = "v %s %s()".formatted(operator, LOCAL_DATE_FUNCTION);
        } else comparison = "Date.parse(v) %s Date.now()".formatted(operator);

        final String defaultMessage = "must be a %sdate%s".formatted(temporal.past() ? "past " : "future ", temporal.orPresent() ? " or the present" : "");
        return refine("v => " + comparison, message(temporal.message(), defaultMessage));
    }

    /**
     * {@return the user message, or the default one if {@link #defaultMessages} are enabled}
     */
    private Optional<String> message(final Optional<String> userMessage, final String defaultMessage) {
        return userMessage.or(() -> defaultMessages ? Optional.of(defaultMessage) : Optional.empty());
    }

    private static String refine(final String function, final Optional<String> message) {
        return call("refine", function, message);
    }

    /**
     * {@return a method call with an argument and an optional error message}
     * @param method the method name
     * @param argument the argument code
     * @param message the error message, if any
     */
    static String call(final String method, final String argument, final Optional<String> message) {
        final String options = message.map(msg -> ", " + options(msg)).orElse("");
        return ".%s(%s%s)".formatted(method, argument, options);
    }

    /**
     * {@return the Zod options object with an error message}, such as {@code { error: "Required" }}
     * @param message the error message
     */
    static String options(final String message) {
        return "{ error: %s }".formatted(JsStrings.quote(message));
    }

    private static String number(final BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
