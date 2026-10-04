package io.github.manoelcampos.java2ts.ts;

import static java.util.Objects.requireNonNull;

/**
 * A TypeScript array type, such as {@code string[]}.
 * @param elementType the type of the array elements
 * @author Manoel Campos
 */
public record TsArrayType(TsType elementType) implements TsType {
    /**
     * Creates a {@link TsArrayType}, validating the components and making immutable copies of collections.
     */
    public TsArrayType {
        requireNonNull(elementType);
    }

    @Override
    public String format() {
        final String element = elementType.format();
        return element.contains(" | ") ? "(%s)[]".formatted(element) : element + "[]";
    }
}
