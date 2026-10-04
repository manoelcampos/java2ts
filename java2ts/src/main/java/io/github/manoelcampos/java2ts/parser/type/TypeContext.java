package io.github.manoelcampos.java2ts.parser.type;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Information shared by the {@link TypeMappingRule}s during a conversion.
 *
 * @param discovery receives the classes referenced by converted types, which must be declared in TypeScript too
 * @param exclusion checks if a class is excluded from conversion
 * @param nullableAnnotations fully qualified names of annotations that make a type nullable
 * @author Manoel Campos
 */
public record TypeContext(Consumer<Class<?>> discovery, Predicate<Class<?>> exclusion, Set<String> nullableAnnotations) {
    /**
     * Creates a {@link TypeContext}, validating the components and making immutable copies of collections.
     */
    public TypeContext {
        nullableAnnotations = Set.copyOf(nullableAnnotations);
    }
}
