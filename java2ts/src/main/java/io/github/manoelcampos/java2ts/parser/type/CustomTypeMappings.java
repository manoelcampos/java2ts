package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converts Java types to TypeScript using mappings defined by the user,
 * such as {@code java.util.List<java.math.BigDecimal>: number[]}.
 *
 * <p>A mapping with generic type arguments (even nested ones) only matches that exact parameterized type.
 * For instance, {@code java.util.List<java.math.BigDecimal>} matches neither {@code List<String>} nor a raw {@code List}.
 * A mapping without type arguments (such as {@code java.util.List}) matches the type with any arguments.</p>
 *
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class CustomTypeMappings<R> implements TypeMappingRule<R> {
    private static final char SEPARATOR = ':';
    private final Map<String, String> mappings;

    /**
     * Creates the custom type mappings.
     * @param mappings a map where each key is a Java type name and the value is the TypeScript type
     */
    public CustomTypeMappings(final Map<String, String> mappings) {
        this.mappings = mappings.entrySet().stream()
                                .collect(Collectors.toMap(entry -> TypeNames.normalize(entry.getKey()), entry -> entry.getValue().strip()));
    }

    /**
     * Parses mappings in the format {@code javaType:tsType}, such as {@code java.util.List[java.math.BigDecimal]:number[]}.
     * @param mappings the mappings to parse
     * @return a map where each key is a Java type name and the value is the TypeScript type
     * @throws IllegalArgumentException if any mapping is not in the expected format
     */
    public static Map<String, String> parse(final List<String> mappings) {
        final var map = new LinkedHashMap<String, String>();
        mappings.forEach(mapping -> {
            final int index = mapping.indexOf(SEPARATOR);
            if (index <= 0 || index == mapping.length() - 1)
                throw new IllegalArgumentException("Invalid custom type mapping '%s'. Expected format: javaType:tsType".formatted(mapping));

            map.put(mapping.substring(0, index).strip(), mapping.substring(index + 1).strip());
        });

        return map;
    }

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return find(type.getType()).map(mapper.renderer()::custom);
    }

    /**
     * {@return the mapping for a Java type, or an empty Optional if there is none}
     * A mapping for the exact (parameterized) type is preferred over a mapping for its raw class.
     * @param javaType the type to look for a mapping
     */
    public Optional<String> find(final Type javaType) {
        final Optional<String> exactMapping = Optional.ofNullable(mappings.get(TypeNames.of(javaType)));
        return exactMapping.or(() -> rawTypeMapping(javaType));
    }

    private Optional<String> rawTypeMapping(final Type javaType) {
        return AnnotatedTypes.rawClass(javaType).map(rawClass -> mappings.get(TypeNames.of(rawClass)));
    }
}
