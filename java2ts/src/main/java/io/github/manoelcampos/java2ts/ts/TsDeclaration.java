package io.github.manoelcampos.java2ts.ts;

import java.util.List;

/**
 * A named TypeScript declaration that is exported by the generated file.
 * @author Manoel Campos
 */
public sealed interface TsDeclaration permits TsInterface, TsTypeAlias {
    /**
     * {@return the name of the declared type}
     */
    String name();

    /**
     * {@return the TypeScript code of the declaration}
     */
    String format();

    /**
     * {@return the order in which this kind of declaration is written into the file,
     * so that all declarations of the same kind are kept together}
     */
    int kindOrder();

    /**
     * Formats the generic type parameters of a declaration.
     * @param typeParameters the names of the type parameters
     * @return the type parameters inside angle brackets, or an empty string if there are none
     */
    static String formatTypeParameters(final List<String> typeParameters) {
        return typeParameters.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParameters));
    }
}
