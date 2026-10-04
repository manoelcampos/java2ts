package io.github.manoelcampos.java2ts.scan;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility methods to navigate the type hierarchy of a class.
 * @author Manoel Campos
 */
public final class TypeHierarchy {
    private TypeHierarchy() {/**/}

    /**
     * {@return a stream of all superclasses and interfaces of a class (direct or indirect),
     * which may contain duplicates}
     * @param aClass the class to get the supertypes
     */
    public static Stream<Class<?>> supertypes(final Class<?> aClass) {
        final Stream<Class<?>> direct = Stream.concat(Stream.ofNullable(aClass.getSuperclass()), Arrays.stream(aClass.getInterfaces()));
        return direct.filter(Objects::nonNull).flatMap(type -> Stream.concat(Stream.of(type), supertypes(type)));
    }

    /**
     * {@return true if a class has a given supertype, false otherwise}
     * @param aClass the class to check
     * @param superTypeName the fully qualified name of the supertype
     */
    public static boolean hasSupertype(final Class<?> aClass, final String superTypeName) {
        return supertypes(aClass).anyMatch(type -> type.getName().equals(superTypeName));
    }
}
