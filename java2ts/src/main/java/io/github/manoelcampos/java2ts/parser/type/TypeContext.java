package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsNullableType;
import io.github.manoelcampos.java2ts.config.NullabilityDefinition;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Information shared by the {@link TypeMappingRule}s during a conversion.
 *
 * @param discovery receives the classes referenced by converted types, which must be declared in TypeScript too
 * @param exclusion checks if a class is excluded from conversion
 * @param nullableAnnotations fully qualified names of annotations that make a type nullable
 * @param nullability how nullable types are written
 * @author Manoel Campos
 */
public record TypeContext(
    Consumer<Class<?>> discovery, Predicate<Class<?>> exclusion,
    Set<String> nullableAnnotations, NullabilityDefinition nullability)
{
    /**
     * Creates a {@link TypeContext}, validating the components and making immutable copies of collections.
     */
    public TypeContext {
        nullableAnnotations = Set.copyOf(nullableAnnotations);
    }

    /**
     * {@return a nullable version of a type, according to the {@link #nullability()} definition}
     * @param type the type to make nullable
     */
    public TsType nullable(final TsType type) {
        return TsNullableType.of(type, nullability);
    }
}
