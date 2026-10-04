package io.github.manoelcampos.java2ts.scan;

import java.util.regex.Pattern;

/**
 * A glob pattern to match fully qualified class names, where:
 * <ul>
 *     <li>{@code **} matches any chars;</li>
 *     <li>{@code *} matches any chars inside a single package or class name
 *         (it doesn't match {@code .} or the {@code $} separator of nested classes).</li>
 * </ul>
 * For instance, {@code com.company.model.**} matches all classes inside the {@code com.company.model}
 * package and its sub-packages, while {@code com.company.model.*DTO} matches only the classes
 * ending with DTO directly inside that package.
 *
 * @param glob the glob pattern
 * @param regex the regular expression equivalent to the glob
 * @author Manoel Campos
 */
public record ClassPattern(String glob, Pattern regex) {
    private static final String ANY_CHARS_PLACEHOLDER = "\u0000";

    /**
     * Creates a pattern from a glob.
     * @param glob the glob pattern
     */
    public ClassPattern(final String glob) {
        this(glob, toRegex(glob.strip()));
    }

    private static Pattern toRegex(final String glob) {
        final String regex = glob.replace("**", ANY_CHARS_PLACEHOLDER)
                                 .chars()
                                 .mapToObj(ClassPattern::charToRegex)
                                 .reduce("", String::concat);
        return Pattern.compile(regex);
    }

    private static String charToRegex(final int ch) {
        return switch (ch) {
            case '\u0000' -> ".*";
            case '*' -> "[^.$]*";
            default -> Pattern.quote(Character.toString(ch));
        };
    }

    /**
     * {@return true if a class name matches this pattern, false otherwise}
     * @param className the fully qualified (binary) name of the class
     */
    public boolean matches(final String className) {
        return regex.matcher(className).matches();
    }
}
