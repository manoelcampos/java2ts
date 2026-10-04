package io.github.manoelcampos.java2ts.ts;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A reference to a declared TypeScript type (such as an interface or type alias),
 * which may have generic type arguments, such as {@code Page<Person>}.
 * Type variables (such as {@code T}) are references without arguments.
 *
 * @param name the name of the referenced type
 * @param typeArguments the generic type arguments
 * @author Manoel Campos
 */
public record TsReferenceType(String name, List<TsType> typeArguments) implements TsType {
    /**
     * Creates a {@link TsReferenceType}, validating the components and making immutable copies of collections.
     */
    public TsReferenceType {
        requireNonNull(name);
        typeArguments = List.copyOf(typeArguments);
    }

    /**
     * Creates a reference without type arguments.
     * @param name the name of the referenced type
     */
    public TsReferenceType(final String name) {
        this(name, List.of());
    }

    @Override
    public String format() {
        if (typeArguments.isEmpty())
            return name;

        final String args = typeArguments.stream().map(TsType::format).collect(Collectors.joining(", "));
        return "%s<%s>".formatted(name, args);
    }
}
