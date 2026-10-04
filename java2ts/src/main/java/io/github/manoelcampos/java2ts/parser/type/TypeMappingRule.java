package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * A rule that converts some kinds of Java types to TypeScript.
 * Rules are tried in order by the {@link TypeMapper}, until one of them is able to convert a type
 * (Chain of Responsibility pattern).
 * @author Manoel Campos
 */
@FunctionalInterface
public interface TypeMappingRule {
    /**
     * Tries to convert a Java type to TypeScript.
     * @param type the Java type to convert (including its type annotations)
     * @param mapper the mapper used to convert any inner types (such as generic type arguments)
     * @return an Optional with the TypeScript type, or an empty Optional if this rule doesn't apply to the given type
     */
    Optional<TsType> map(AnnotatedType type, TypeMapper mapper);
}
