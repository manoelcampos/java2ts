package io.github.manoelcampos.java2ts.ts;

import java.util.List;

/**
 * A TypeScript type, which knows how to write itself as TypeScript code.
 * @author Manoel Campos
 */
public sealed interface TsType permits TsBasicType, TsArrayType, TsReferenceType, TsUnionType, TsMapType, TsNullableType {
    /** The {@code any} type, used for types that cannot (or must not) be converted. */
    TsType ANY = new TsBasicType("any");

    /**
     * {@return the TypeScript code representing this type}
     */
    String format();

    /**
     * {@return the types that compose this type when it is part of a union}
     * A type that isn't a union is composed just by itself.
     */
    default List<TsType> unionMembers() {
        return List.of(this);
    }

    /**
     * {@return true if this type explicitly includes a basic type (such as {@code null} or {@code undefined}), false otherwise}
     * For instance, {@code string | null} and {@code Nullable<string>} (for a {@code T | null} alias) include {@code null}.
     * @param typeName the name of the basic type to check
     */
    default boolean includes(final String typeName) {
        return switch (this) {
            case TsBasicType basic -> basic.name().equals(typeName);
            case TsUnionType union -> union.types().stream().anyMatch(type -> type.includes(typeName));
            case TsNullableType nullable -> nullable.definition().types().contains(typeName) || nullable.type().includes(typeName);
            default -> false;
        };
    }
}
