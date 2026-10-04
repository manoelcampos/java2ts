package io.github.manoelcampos.java2ts.javadoc;

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Cleans up JavaDoc comments read from the xml-doclet output, making them suitable for JSDoc.
 * @author Manoel Campos
 */
final class CommentCleaner {
    /** The xml-doclet surrounds inline tags with commas, such as {@code ,{@link Foo},}. */
    private static final Pattern INLINE_TAG_WITH_COMMAS = Pattern.compile(",(\\{@[^}]*}),");
    private static final Pattern CODE_TAG = Pattern.compile("\\{@(?:code|literal)\\s+([^}]*)}");
    private static final Pattern RETURN_TAG = Pattern.compile("\\{@return\\s+([^}]*)}");

    private CommentCleaner() {/**/}

    /**
     * {@return a cleaned up comment, with each line trimmed and inline tags fixed}
     * {@code {@code x}} and {@code {@literal x}} are converted to Markdown code, and
     * {@code {@return x}} is converted to "Returns x".
     * @param comment the comment to clean
     */
    static String clean(final @Nullable String comment) {
        if (comment == null)
            return "";

        String text = INLINE_TAG_WITH_COMMAS.matcher(comment).replaceAll("$1");
        text = CODE_TAG.matcher(text).replaceAll("`$1`");
        text = RETURN_TAG.matcher(text).replaceAll("Returns $1");
        return text.lines().map(String::strip).reduce((a, b) -> a + "\n" + b).orElse("").strip();
    }

    /**
     * Removes the tag name from the beginning of a tag text,
     * since the xml-doclet includes it (such as {@code @author Manoel} for the author tag).
     * @param tagName the tag name, without the {@code @}
     * @param text the tag text
     * @return the tag text without the tag name
     */
    static String tagText(final String tagName, final String text) {
        final String prefix = "@" + tagName;
        final String withoutName = text.strip().startsWith(prefix) ? text.strip().substring(prefix.length()) : text;
        return clean(withoutName);
    }
}
