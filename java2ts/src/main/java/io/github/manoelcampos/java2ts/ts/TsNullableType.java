package io.github.manoelcampos.java2ts.ts;

import io.github.manoelcampos.java2ts.config.NullabilityDefinition;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A nullable TypeScript type, written according to a {@link NullabilityDefinition}:
 * either as an inline union (such as {@code string | null})
 * or using the {@link #ALIAS_NAME} type alias (such as {@code Nullable<string>}).
 *
 * @param type the type that is nullable
 * @param definition how the nullable type is written
 * @author Manoel Campos
 */
public record TsNullableType(TsType type, NullabilityDefinition definition) implements TsType {
    /** Name of the type alias used for non-inline {@link NullabilityDefinition}s. */
    public static final String ALIAS_NAME = "Nullable";

    /**
     * Creates a {@link TsNullableType}, validating the components and making immutable copies of collections.
     */
    public TsNullableType {
        requireNonNull(type);
        requireNonNull(definition);
    }

    /**
     * Makes a type nullable, unless it already is.
     * @param type the type to make nullable
     * @param definition how the nullable type is written
     * @return the nullable type
     */
    public static TsType of(final TsType type, final NullabilityDefinition definition) {
        return type instanceof TsNullableType ? type : new TsNullableType(type, definition);
    }

    /**
     * {@return the declaration of the {@link #ALIAS_NAME} type alias for a given nullability definition}
     * @param definition the nullability definition defining which types the alias includes
     */
    public static TsTypeAlias aliasDeclaration(final NullabilityDefinition definition) {
        final var typeVariable = new TsReferenceType("T");
        return new TsTypeAlias(ALIAS_NAME, List.of(typeVariable.name()), TsUnionType.combine(typeVariable, definition.types()), "");
    }

    @Override
    public List<TsType> unionMembers() {
        return definition.isInline() ? TsUnionType.combine(type, definition.types()).unionMembers() : List.of(this);
    }

    @Override
    public String format() {
        return definition.isInline() ? TsUnionType.of(unionMembers()).format() : "%s<%s>".formatted(ALIAS_NAME, type.format());
    }
}
