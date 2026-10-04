package io.github.manoelcampos.java2ts.ts;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A property of a TypeScript interface.
 * @param name the property name
 * @param type the property type
 * @param optional if the property is declared with a question mark (such as {@code name?: string})
 * @param comment the property documentation (may be empty)
 * @author Manoel Campos
 */
public record TsProperty(String name, TsType type, boolean optional, String comment) {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final String INDENT = "    ";

    /**
     * Creates a {@link TsProperty}, validating the components and making immutable copies of collections.
     */
    public TsProperty {
        requireNonNull(name);
        requireNonNull(type);
        comment = requireNonNullElse(comment, "");
    }

    /**
     * {@return the TypeScript code of the property, including its documentation}
     */
    public String format() {
        final String quotedName = IDENTIFIER.matcher(name).matches() ? name : '"' + name + '"';
        return "%s%s%s%s: %s;".formatted(JsDoc.format(comment, INDENT), INDENT, quotedName, optional ? "?" : "", type.format());
    }
}
