package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.ts.TsModel;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The result of converting Java classes to TypeScript.
 *
 * @param tsModel the TypeScript declarations
 * @param declaredClasses the Java classes that got a TypeScript declaration,
 *                        including the ones referenced by the converted classes
 * @author Manoel Campos
 */
public record ParsedModel(TsModel tsModel, List<Class<?>> declaredClasses) {
    /**
     * Creates a {@link ParsedModel}, validating the components and making immutable copies of collections.
     */
    public ParsedModel {
        requireNonNull(tsModel);
        declaredClasses = List.copyOf(declaredClasses);
    }
}
