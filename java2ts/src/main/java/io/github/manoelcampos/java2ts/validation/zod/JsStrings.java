package io.github.manoelcampos.java2ts.validation.zod;

import java.util.regex.Pattern;

/**
 * Utility methods to write JavaScript literals.
 * @author Manoel Campos
 */
final class JsStrings {
    /** Unicode line terminators that aren't allowed unescaped in some JavaScript engines. */
    private static final char LINE_SEPARATOR = 0x2028;
    private static final char PARAGRAPH_SEPARATOR = 0x2029;
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    private JsStrings() {/**/}

    /**
     * {@return a JavaScript string literal (inside double quotes) with the given text}
     * @param text the text to quote
     */
    static String quote(final String text) {
        final var builder = new StringBuilder("\"");
        text.chars().forEach(ch -> builder.append(escape((char) ch)));
        return builder.append('"').toString();
    }

    private static String escape(final char ch) {
        return switch (ch) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> ch == LINE_SEPARATOR || ch == PARAGRAPH_SEPARATOR ? "\\u%04x".formatted((int) ch) : String.valueOf(ch);
        };
    }

    /**
     * {@return a name to be used as an object key, which is quoted only if it isn't a valid identifier}
     * @param name the key name
     */
    static String key(final String name) {
        return IDENTIFIER.matcher(name).matches() ? name : quote(name);
    }
}
