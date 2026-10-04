package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsMapType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts any {@link Map} to a TypeScript {@code Record}, such as {@code Map<String, Integer>} to {@code Record<string, number>}.
 * Keys that are numbers are kept as numbers, enum keys produce a partial record
 * (such as {@code Partial<Record<Color, number>>}) and any other key type is converted to string
 * (since JSON object keys are strings).
 * Raw maps are converted to {@code Record<string, any>}.
 * @author Manoel Campos
 */
public final class MapRule implements TypeMappingRule {
    /**
     * Creates a {@link MapRule}.
     */
    public MapRule() {/**/}

    private static final int KEY_TYPE_ARG_INDEX = 0;
    private static final int VALUE_TYPE_ARG_INDEX = 1;

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(Map.class::isAssignableFrom)
                             .map(mapClass -> recordType(AnnotatedTypes.typeArguments(type), mapper));
    }

    private static TsType recordType(final List<AnnotatedType> args, final TypeMapper mapper) {
        if (args.size() <= VALUE_TYPE_ARG_INDEX)
            return new TsMapType(TsBasicType.STRING, TsType.ANY, false);

        final AnnotatedType keyType = args.get(KEY_TYPE_ARG_INDEX);
        final TsType valueType = mapper.map(args.get(VALUE_TYPE_ARG_INDEX));
        if (isEnum(keyType))
            return new TsMapType(mapper.map(keyType), valueType, true);

        final TsType tsKeyType = TsBasicType.NUMBER.equals(mapper.map(keyType)) ? TsBasicType.NUMBER : TsBasicType.STRING;
        return new TsMapType(tsKeyType, valueType, false);
    }

    private static boolean isEnum(final AnnotatedType type) {
        return AnnotatedTypes.rawClass(type.getType()).filter(Class::isEnum).isPresent();
    }
}
