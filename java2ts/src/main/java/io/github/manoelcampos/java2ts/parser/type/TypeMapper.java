package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Converts Java types to TypeScript, trying a list of {@link TypeMappingRule}s in order.
 * Types annotated with any of the {@link TypeContext#nullableAnnotations()}
 * (including generic type arguments, such as {@code List<@Nullable String>}) become nullable.
 * @author Manoel Campos
 */
public final class TypeMapper {
    private final List<TypeMappingRule> rules;
    private final TypeContext context;

    /**
     * Creates a type mapper.
     * @param context the information shared by the rules
     * @param rules the rules to try, in order
     */
    public TypeMapper(final TypeContext context, final List<TypeMappingRule> rules) {
        this.context = context;
        this.rules = List.copyOf(rules);
    }

    /**
     * {@return the information shared by the rules}
     */
    public TypeContext context() {
        return context;
    }

    /**
     * Converts a Java type to TypeScript.
     * @param type the type to convert
     * @return the TypeScript type, or {@link TsType#ANY} if no rule can convert the type
     */
    public TsType map(final AnnotatedType type) {
        final TsType tsType = rules.stream()
                                   .map(rule -> rule.map(type, this))
                                   .flatMap(Optional::stream)
                                   .findFirst()
                                   .orElse(TsType.ANY);

        return isNullable(type) ? context.nullable(tsType) : tsType;
    }

    private boolean isNullable(final AnnotatedType type) {
        return Arrays.stream(type.getAnnotations())
                     .anyMatch(annotation -> context.nullableAnnotations().contains(annotation.annotationType().getName()));
    }
}
