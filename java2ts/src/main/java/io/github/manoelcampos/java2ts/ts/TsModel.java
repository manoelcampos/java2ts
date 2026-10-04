package io.github.manoelcampos.java2ts.ts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All the TypeScript declarations to be written into the generated file.
 * Declarations are sorted by kind (interfaces first, then type aliases) and then by name,
 * so that the generated file only changes when the Java classes change.
 *
 * @param declarations the TypeScript declarations
 * @author Manoel Campos
 */
public record TsModel(List<TsDeclaration> declarations) {
    private static final Comparator<TsDeclaration> ORDER =
        Comparator.comparingInt(TsDeclaration::kindOrder).thenComparing(TsDeclaration::name);

    /**
     * Creates a {@link TsModel}, validating the components and making immutable copies of collections.
     */
    public TsModel {
        declarations = declarations.stream().sorted(ORDER).toList();
    }

    /**
     * {@return the TypeScript code of all declarations, separated by blank lines}
     */
    public String format() {
        return declarations.stream().map(TsDeclaration::format).collect(Collectors.joining("\n"));
    }
}
