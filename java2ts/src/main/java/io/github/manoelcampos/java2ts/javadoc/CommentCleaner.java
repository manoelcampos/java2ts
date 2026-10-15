package io.github.manoelcampos.java2ts.javadoc;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Cleans up JavaDoc comments read from the xml-doclet output, making them suitable for JSDoc.
 * @author Manoel Campos
 */
final class CommentCleaner {
    /**
     * The separator the xml-doclet adds between two nodes of a comment that are not HTML elements.
     * Since text nodes are never adjacent, the separator is always right before or right after an inline tag,
     * such as {@code Use ,{@link Foo}, here} or {@code <li>{@link Foo}, text}.
     */
    private static final char DOCLET_SEPARATOR = ',';
    private static final Set<String> CODE_TAGS = Set.of("code", "literal");
    private static final Set<String> RETURN_TAGS = Set.of("return");

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

        String text = removeDocletSeparators(comment);
        text = replaceTags(text, CODE_TAGS, content -> "`" + content + "`");
        text = replaceTags(text, RETURN_TAGS, content -> "Returns " + content);
        return text.lines().map(String::strip).reduce((a, b) -> a + "\n" + b).orElse("").strip();
    }

    /**
     * {@return a comment without the separators the xml-doclet adds around inline tags}
     * One separator is removed right before and one right after each inline tag (when present),
     * so that commas written in the original comment are kept.
     * @param comment the comment to remove the separators from
     */
    private static String removeDocletSeparators(final String comment) {
        final var result = new StringBuilder(comment.length());
        int pos = 0;
        for (Optional<InlineTag> tag = InlineTag.next(comment, pos); tag.isPresent(); tag = InlineTag.next(comment, pos)) {
            result.append(comment, pos, tag.get().start());
            removeTrailingSeparator(result);
            result.append(comment, tag.get().start(), tag.get().end());
            pos = skipSeparator(comment, tag.get().end());
        }

        return result.append(comment.substring(pos)).toString();
    }

    /**
     * Removes the separator at the end of a text, if there is one.
     * @param text the text to change
     */
    private static void removeTrailingSeparator(final StringBuilder text) {
        final int last = text.length() - 1;
        if (last >= 0 && text.charAt(last) == DOCLET_SEPARATOR)
            text.deleteCharAt(last);
    }

    /**
     * {@return the index right after the separator at a given index, or that same index if there is no separator there}
     * @param comment the comment to check
     * @param index the index where the separator may be
     */
    private static int skipSeparator(final String comment, final int index) {
        return index < comment.length() && comment.charAt(index) == DOCLET_SEPARATOR ? index + 1 : index;
    }

    /**
     * Replaces some inline tags in a comment by a text built from their content.
     * Tags with other names are kept, but tags inside them are replaced too.
     * @param comment the comment to replace the tags in
     * @param tagNames the names of the tags to replace
     * @param replacement a function that receives the content of a tag and builds the text that replaces the whole tag
     * @return the comment with the tags replaced
     */
    private static String replaceTags(final String comment, final Set<String> tagNames, final UnaryOperator<String> replacement) {
        final var result = new StringBuilder(comment.length());
        int pos = 0;
        for (Optional<InlineTag> tag = InlineTag.next(comment, pos); tag.isPresent(); tag = InlineTag.next(comment, pos)) {
            pos = appendTag(comment, pos, tag.get(), tagNames, replacement, result);
        }

        return result.append(comment.substring(pos)).toString();
    }

    /**
     * Appends the text before a tag and the tag itself (replaced if it has one of the given names) to a result.
     * @param comment the comment containing the tag
     * @param pos the index in the comment where the text not yet appended starts
     * @param tag the tag to append
     * @param tagNames the names of the tags to replace
     * @param replacement a function that receives the content of a tag and builds the text that replaces the whole tag
     * @param result where to append the text
     * @return the index in the comment where the text not yet appended starts after this call.
     *         For a tag that is kept, that is right after its opening brace, so that tags inside it are replaced too.
     */
    private static int appendTag(
        final String comment, final int pos, final InlineTag tag,
        final Set<String> tagNames, final UnaryOperator<String> replacement, final StringBuilder result)
    {
        result.append(comment, pos, tag.start());
        if (tagNames.contains(tag.name())) {
            result.append(replacement.apply(tag.content()));
            return tag.end();
        }

        result.append(comment.charAt(tag.start()));
        return tag.start() + 1;
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
