package io.github.manoelcampos.java2ts.ts;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A TypeScript type alias declaration, such as {@code export type Color = "RED" | "BLUE";},
 * used to represent Java enums.
 * @param name the alias name
 * @param typeParameters the names of the generic type parameters
 * @param type the aliased type
 * @param comment the alias documentation (may be empty)
 * @author Manoel Campos
 */
public record TsTypeAlias(String name, List<String> typeParameters, TsType type, String comment) implements TsDeclaration {
    /**
     * Creates a {@link TsTypeAlias}, validating the components and making immutable copies of collections.
     */
    public TsTypeAlias {
        requireNonNull(name);
        requireNonNull(type);
        typeParameters = List.copyOf(typeParameters);
        comment = requireNonNullElse(comment, "");
    }

    @Override
    public String format() {
        return "%sexport type %s%s = %s;\n".formatted(
            JsDoc.format(comment, ""), name, TsDeclaration.formatTypeParameters(typeParameters), type.format());
    }

    @Override
    public int kindOrder() {
        return 1;
    }
}
