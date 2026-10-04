package io.github.manoelcampos.java2ts.ts;

import org.jspecify.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * Formats documentation comments as JSDoc blocks.
 * @author Manoel Campos
 */
public final class JsDoc {
    private JsDoc() {/**/}

    /**
     * Formats a documentation text as a JSDoc block.
     * @param text the documentation text, which may have multiple lines
     * @param indent the indentation to add before each line of the block
     * @return the JSDoc block ending with a line break, or an empty string if the text is blank
     */
    public static String format(final @Nullable String text, final String indent) {
        if (text == null || text.isBlank())
            return "";

        final String body = text.strip()
                                .replace("*/", "*\\/")
                                .lines()
                                .map(line -> (indent + " * " + line.strip()).stripTrailing())
                                .collect(Collectors.joining("\n"));

        return "%s/**\n%s\n%s */\n".formatted(indent, body, indent);
    }
}
