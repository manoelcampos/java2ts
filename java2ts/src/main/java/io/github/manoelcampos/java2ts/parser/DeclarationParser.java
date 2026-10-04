package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.ts.TsDeclaration;

/**
 * Converts a Java class into a TypeScript declaration (Strategy pattern).
 * @author Manoel Campos
 */
public sealed interface DeclarationParser permits EnumDeclarationParser, InterfaceDeclarationParser {
    /**
     * {@return the TypeScript declaration for a Java class}
     * @param aClass the class to convert
     */
    TsDeclaration parse(Class<?> aClass);
}
