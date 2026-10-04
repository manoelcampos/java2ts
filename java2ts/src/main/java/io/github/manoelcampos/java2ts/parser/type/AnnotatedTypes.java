package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Utility methods to work with Java {@link Type}s and {@link AnnotatedType}s.
 * @author Manoel Campos
 */
public final class AnnotatedTypes {
    private AnnotatedTypes() {/**/}

    /**
     * {@return the raw class of a type (such as {@code List} for {@code List<String>}),
     * or an empty Optional for type variables and wildcards}
     * @param type the type to get the raw class
     */
    public static Optional<Class<?>> rawClass(final Type type) {
        return switch (type) {
            case Class<?> aClass -> Optional.of(aClass);
            case ParameterizedType parameterized -> rawClass(parameterized.getRawType());
            case GenericArrayType array -> rawClass(array.getGenericComponentType()).map(Class::arrayType);
            default -> Optional.empty();
        };
    }

    /**
     * {@return the generic type arguments of a type, or an empty list if it is not a parameterized type}
     * @param type the type to get the type arguments
     */
    public static List<AnnotatedType> typeArguments(final AnnotatedType type) {
        return type instanceof AnnotatedParameterizedType parameterized ?
                List.of(parameterized.getAnnotatedActualTypeArguments()) :
                List.of();
    }
}
