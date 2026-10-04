package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Converts Java regular expressions (such as the ones in {@code @Pattern}) into JavaScript regular expression literals.
 * Since Java's {@code @Pattern} must match the whole value, the expression is anchored with {@code ^} and {@code $}.
 * Java features that JavaScript doesn't have fail the conversion, instead of producing a different validation.
 * @author Manoel Campos
 */
public final class JsRegex {
    /** JavaScript flags for the {@code Pattern.Flag}s that have one. */
    private static final Map<String, String> FLAGS = Map.of("CASE_INSENSITIVE", "i", "MULTILINE", "m", "DOTALL", "s");

    /** {@code Pattern.Flag}s that don't change how the expressions used in validation behave in JavaScript. */
    private static final Set<String> IGNORED_FLAGS = Set.of("UNICODE_CASE", "UNIX_LINES");

    /** Java regular expression features that JavaScript doesn't have. */
    private static final List<Pattern> UNSUPPORTED_SYNTAX = List.of(
        Pattern.compile("(?<!\\\\)\\\\[AZzG]"),       // \A, \Z, \z and \G boundaries
        Pattern.compile("\\(\\?>"),                    // atomic groups
        Pattern.compile("\\(\\?[a-zA-Z]+[-a-zA-Z]*[):]"), // inline flags, such as (?i)
        Pattern.compile("(?<!\\\\)[*+?]\\+"),           // possessive quantifiers, such as a++
        Pattern.compile("\\{\\d+(,\\d*)?}\\+")            // possessive quantifiers, such as a{2}+
    );

    private static final Pattern UNICODE_PROPERTY = Pattern.compile("\\\\[pP]\\{");
    private static final String UNICODE_FLAG = "u";

    private JsRegex() {/**/}

    /**
     * Converts a Java regular expression into a JavaScript regular expression literal.
     * @param regex the Java regular expression
     * @param flags names of the {@code Pattern.Flag}s
     * @return the JavaScript literal, such as {@code /^(?:[a-z]+)$/i}
     * @throws UnsupportedValidationException if the expression uses features that JavaScript doesn't have
     */
    public static String of(final String regex, final Set<String> flags) {
        check(regex, flags);
        return "/^(?:%s)$/%s".formatted(escape(regex), jsFlags(regex, flags));
    }

    /**
     * Checks if a Java regular expression can be converted to JavaScript.
     * @param regex the Java regular expression
     * @param flags names of the {@code Pattern.Flag}s
     * @throws UnsupportedValidationException if the expression uses features that JavaScript doesn't have
     */
    public static void check(final String regex, final Set<String> flags) {
        checkSyntax(regex);
        jsFlags(regex, flags);
    }

    private static void checkSyntax(final String regex) {
        if (UNSUPPORTED_SYNTAX.stream().anyMatch(pattern -> pattern.matcher(regex).find()))
            throw new UnsupportedValidationException("regular expression %s uses Java features JavaScript doesn't have".formatted(regex));
    }

    private static String jsFlags(final String regex, final Set<String> flags) {
        final String unsupported = flags.stream()
                                        .filter(flag -> !FLAGS.containsKey(flag) && !IGNORED_FLAGS.contains(flag))
                                        .sorted()
                                        .collect(Collectors.joining(", "));
        if (!unsupported.isEmpty())
            throw new UnsupportedValidationException("regular expression flags %s aren't supported".formatted(unsupported));

        final String jsFlags = flags.stream().map(FLAGS::get).filter(Objects::nonNull).sorted().collect(Collectors.joining());
        return UNICODE_PROPERTY.matcher(regex).find() ? jsFlags + UNICODE_FLAG : jsFlags;
    }

    /**
     * {@return the expression with the characters that can't appear in a JavaScript regular expression literal escaped}
     * (unescaped slashes and line breaks)
     */
    private static String escape(final String regex) {
        final var builder = new StringBuilder();
        boolean escaped = false;
        for (final char ch : regex.toCharArray()) {
            builder.append(switch (ch) {
                case '/' -> escaped ? "/" : "\\/";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                default -> String.valueOf(ch);
            });
            escaped = !escaped && ch == '\\';
        }

        return builder.toString();
    }
}
