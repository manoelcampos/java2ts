package io.github.manoelcampos.java2ts.javadoc;

import java.util.Optional;

/**
 * An inline tag found inside a JavaDoc comment, such as {@code {@link Foo}} or {@code {@code a{b}c}}.
 * Like the JavaDoc tool, the tag content may have balanced braces inside it,
 * so that {@code {@code a{b}c}} has the content {@code a{b}c}.
 * @param start the index of the opening {@code {@} of the tag in the comment
 * @param end the index right after the closing brace of the tag in the comment (exclusive)
 * @param name the tag name, without the {@code @}
 * @param content the tag content after the name, without the leading whitespace
 * @author Manoel Campos
 */
record InlineTag(int start, int end, String name, String content) {
    private static final String TAG_START = "{@";
    private static final char OPEN_BRACE = '{';
    private static final char CLOSE_BRACE = '}';

    /**
     * Finds the next inline tag inside a comment.
     * @param comment the comment to search
     * @param from the index to start searching from
     * @return an Optional with the next inline tag that has a matching closing brace,
     *         or an empty Optional if there is no more inline tags
     */
    static Optional<InlineTag> next(final String comment, final int from) {
        for (int start = comment.indexOf(TAG_START, from); start >= 0; start = comment.indexOf(TAG_START, start + 1)) {
            final int end = closingBraceIndex(comment, start);
            if (end >= 0)
                return Optional.of(parse(comment, start, end + 1));
        }

        return Optional.empty();
    }

    /**
     * {@return the index of the brace that closes the tag starting at a given index, or -1 if it's not closed}
     * Braces inside the tag must be balanced.
     * @param comment the comment containing the tag
     * @param start the index of the opening brace of the tag
     */
    private static int closingBraceIndex(final String comment, final int start) {
        int depth = 0;
        for (int i = start; i < comment.length(); i++) {
            final char c = comment.charAt(i);
            if (c == OPEN_BRACE)
                depth++;
            else if (c == CLOSE_BRACE && --depth == 0)
                return i;
        }

        return -1;
    }

    /**
     * {@return an inline tag parsed from a part of a comment}
     * @param comment the comment containing the tag
     * @param start the index of the opening brace of the tag
     * @param end the index right after the closing brace of the tag
     */
    private static InlineTag parse(final String comment, final int start, final int end) {
        final String body = comment.substring(start + TAG_START.length(), end - 1);
        final int nameEnd = nameEndIndex(body);
        return new InlineTag(start, end, body.substring(0, nameEnd), body.substring(nameEnd).stripLeading());
    }

    /**
     * {@return the index right after the tag name, which ends at the first whitespace or at the end of the tag body}
     * @param body the tag text between {@code {@} and the closing brace
     */
    private static int nameEndIndex(final String body) {
        int i = 0;
        while (i < body.length() && !Character.isWhitespace(body.charAt(i)))
            i++;

        return i;
    }
}
