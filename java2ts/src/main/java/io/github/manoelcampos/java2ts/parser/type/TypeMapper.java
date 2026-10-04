package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Converts Java types, trying a list of {@link TypeMappingRule}s in order.
 * Types annotated with any of the {@link TypeContext#nullableAnnotations()}
 * (including generic type arguments, such as {@code List<@Nullable String>}) become nullable.
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class TypeMapper<R> {
    private final List<TypeMappingRule<R>> rules;
    private final TypeContext context;
    private final TypeRenderer<R> renderer;

    /**
     * Creates a type mapper.
     * @param context the information shared by the rules
     * @param renderer creates the results for the types classified by the rules
     * @param rules the rules to try, in order
     */
    public TypeMapper(final TypeContext context, final TypeRenderer<R> renderer, final List<TypeMappingRule<R>> rules) {
        this.context = context;
        this.renderer = renderer;
        this.rules = List.copyOf(rules);
    }

    /**
     * {@return the information shared by the rules}
     */
    public TypeContext context() {
        return context;
    }

    /**
     * {@return the renderer that creates the results for the types classified by the rules}
     */
    public TypeRenderer<R> renderer() {
        return renderer;
    }

    /**
     * Converts a Java type.
     * @param type the type to convert
     * @return the result, which is {@link TypeRenderer#any()} if no rule can convert the type
     */
    public R map(final AnnotatedType type) {
        final R result = rules.stream()
                              .map(rule -> rule.map(type, this))
                              .flatMap(Optional::stream)
                              .findFirst()
                              .orElseGet(renderer::any);

        final R annotated = renderer.annotate(result, type);
        return isNullable(type) ? renderer.nullable(annotated) : annotated;
    }

    private boolean isNullable(final AnnotatedType type) {
        return Arrays.stream(type.getAnnotations())
                     .anyMatch(annotation -> context.nullableAnnotations().contains(annotation.annotationType().getName()));
    }
}
