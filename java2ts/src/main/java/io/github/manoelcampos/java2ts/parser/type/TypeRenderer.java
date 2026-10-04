package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.List;

/**
 * Creates the representation of a Java type for a specific output (Abstract Factory pattern),
 * such as TypeScript types or validation schemas.
 * The {@link TypeMappingRule}s classify Java types and call this renderer to build the result,
 * so that the classification is shared by all outputs.
 * @param <R> the type of the result
 * @author Manoel Campos
 */
public interface TypeRenderer<R> {
    /**
     * {@return the result for a type that accepts any value}
     */
    R any();

    /**
     * {@return the result for a type mapped by the user}
     * @param expression the code given by the user for the type
     */
    R custom(String expression);

    /**
     * {@return the result for a generic type variable}
     * @param name the type variable name (such as {@code T})
     */
    R typeVariable(String name);

    /**
     * {@return the result for a type serialized as a JSON primitive value}
     * @param kind the kind of the type
     */
    R basic(BasicKind kind);

    /**
     * {@return the result for a date/time type}
     * @param kind the kind of the date/time type
     */
    R date(DateKind kind);

    /**
     * {@return the result for an array or collection}
     * @param element the result for the element type
     */
    R array(R element);

    /**
     * {@return the result for a map}
     * @param keyKind the kind of the map keys
     * @param key the result for the key type (only meaningful for {@link MapKeyKind#ENUM} keys)
     * @param value the result for the value type
     */
    R map(MapKeyKind keyKind, R key, R value);

    /**
     * {@return the result for a reference to a declared class}
     * @param aClass the referenced class
     * @param typeArguments the results for the generic type arguments
     */
    R reference(Class<?> aClass, List<R> typeArguments);

    /**
     * {@return a nullable version of a result}
     * @param type the result to make nullable
     */
    R nullable(R type);

    /**
     * Adds information from the annotations of a type usage (such as validation constraints) to a result.
     * By default, the annotations are ignored.
     * @param type the result for the type
     * @param annotatedType the type usage, including its annotations
     * @return the result with the annotations information
     */
    default R annotate(final R type, final AnnotatedType annotatedType) {
        return type;
    }
}
