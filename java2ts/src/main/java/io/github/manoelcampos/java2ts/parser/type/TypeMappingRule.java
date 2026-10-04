package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * A rule that converts some kinds of Java types, using the {@link TypeMapper#renderer()} to build the result.
 * Rules are tried in order by the {@link TypeMapper}, until one of them is able to convert a type
 * (Chain of Responsibility pattern).
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
@FunctionalInterface
public interface TypeMappingRule<R> {
    /**
     * Tries to convert a Java type.
     * @param type the Java type to convert (including its type annotations)
     * @param mapper the mapper used to convert any inner types (such as generic type arguments)
     * @return an Optional with the result, or an empty Optional if this rule doesn't apply to the given type
     */
    Optional<R> map(AnnotatedType type, TypeMapper<R> mapper);
}
