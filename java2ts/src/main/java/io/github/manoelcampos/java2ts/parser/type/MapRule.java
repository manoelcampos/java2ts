package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts any {@link Map}, such as {@code Map<String, Integer>} to the TypeScript {@code Record<string, number>}.
 * Keys that are numbers are kept as numbers, enum keys produce a partial record
 * (such as {@code Partial<Record<Color, number>>}) and any other key type is converted to string
 * (since JSON object keys are strings). Check {@link MapKeyKind}.
 * Raw maps have string keys and values of any type.
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class MapRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link MapRule}.
     */
    public MapRule() {/**/}

    private static final int KEY_TYPE_ARG_INDEX = 0;
    private static final int VALUE_TYPE_ARG_INDEX = 1;

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(Map.class::isAssignableFrom)
                             .map(mapClass -> recordType(AnnotatedTypes.typeArguments(type), mapper));
    }

    private static <R> R recordType(final List<AnnotatedType> args, final TypeMapper<R> mapper) {
        final TypeRenderer<R> renderer = mapper.renderer();
        if (args.size() <= VALUE_TYPE_ARG_INDEX)
            return renderer.map(MapKeyKind.STRING, renderer.any(), renderer.any());

        final AnnotatedType keyType = args.get(KEY_TYPE_ARG_INDEX);
        final R value = mapper.map(args.get(VALUE_TYPE_ARG_INDEX));
        final MapKeyKind keyKind = keyKind(keyType);
        final R key = keyKind == MapKeyKind.ENUM ? mapper.map(keyType) : renderer.any();
        return renderer.map(keyKind, key, value);
    }

    private static MapKeyKind keyKind(final AnnotatedType keyType) {
        final Optional<Class<?>> keyClass = AnnotatedTypes.rawClass(keyType.getType());
        if (keyClass.filter(Class::isEnum).isPresent())
            return MapKeyKind.ENUM;

        final boolean number = keyClass.flatMap(BasicKind::of).filter(BasicKind::isNumber).isPresent();
        return number ? MapKeyKind.NUMBER : MapKeyKind.STRING;
    }
}
