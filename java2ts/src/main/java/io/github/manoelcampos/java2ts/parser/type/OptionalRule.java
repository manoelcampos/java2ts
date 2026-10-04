package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Converts {@link Optional} types to nullable types, such as {@code Optional<String>} to {@code string | null}
 * (since an empty Optional is serialized as null).
 * {@link OptionalInt}, {@link OptionalLong} and {@link OptionalDouble} are converted to nullable numbers.
 *
 * <p>When the Optional is the type of a property, the property is declared as optional
 * (such as {@code name?: string}). Check {@link #isOptional(Class)}.</p>
 * @author Manoel Campos
 */
public final class OptionalRule implements TypeMappingRule {
    /**
     * Creates a {@link OptionalRule}.
     */
    public OptionalRule() {/**/}

    private static final List<Class<?>> NUMBER_OPTIONALS = List.of(OptionalInt.class, OptionalLong.class, OptionalDouble.class);

    /**
     * {@return true if a class is one of the Java Optional types, false otherwise}
     * @param aClass the class to check
     */
    public static boolean isOptional(final Class<?> aClass) {
        return aClass == Optional.class || NUMBER_OPTIONALS.contains(aClass);
    }

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(OptionalRule::isOptional)
                             .map(optionalClass -> mapper.context().nullable(valueType(optionalClass, type, mapper)));
    }

    private static TsType valueType(final Class<?> optionalClass, final AnnotatedType type, final TypeMapper mapper) {
        if (NUMBER_OPTIONALS.contains(optionalClass))
            return TsBasicType.NUMBER;

        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        return args.isEmpty() ? TsType.ANY : mapper.map(args.getFirst());
    }
}
