package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Map;
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
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class OptionalRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link OptionalRule}.
     */
    public OptionalRule() {/**/}

    private static final Map<Class<?>, BasicKind> NUMBER_OPTIONALS = Map.of(
        OptionalInt.class, BasicKind.INTEGER,
        OptionalLong.class, BasicKind.INTEGER,
        OptionalDouble.class, BasicKind.DECIMAL
    );

    /**
     * {@return true if a class is one of the Java Optional types, false otherwise}
     * @param aClass the class to check
     */
    public static boolean isOptional(final Class<?> aClass) {
        return aClass == Optional.class || NUMBER_OPTIONALS.containsKey(aClass);
    }

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(OptionalRule::isOptional)
                             .map(optionalClass -> mapper.renderer().nullable(valueType(optionalClass, type, mapper)));
    }

    private static <R> R valueType(final Class<?> optionalClass, final AnnotatedType type, final TypeMapper<R> mapper) {
        final BasicKind numberKind = NUMBER_OPTIONALS.get(optionalClass);
        if (numberKind != null)
            return mapper.renderer().basic(numberKind);

        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        return args.isEmpty() ? mapper.renderer().any() : mapper.map(args.getFirst());
    }
}
