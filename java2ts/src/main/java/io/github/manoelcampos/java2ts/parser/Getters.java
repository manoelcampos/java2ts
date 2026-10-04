package io.github.manoelcampos.java2ts.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Identifies getter methods and the names of the properties they represent,
 * following the same rules as Jackson.
 * @author Manoel Campos
 */
final class Getters {
    private static final String GET = "get";
    private static final String IS = "is";

    private Getters() {/**/}

    /**
     * {@return true if a method is a getter, false otherwise}
     * A getter is a public, non-static method without parameters, whose name starts with
     * {@code get} (and returns a value) or {@code is} (and returns a primitive boolean).
     * {@link Object#getClass()} is not considered a getter.
     * @param method the method to check
     */
    static boolean isGetter(final Method method) {
        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() > 0 || method.isBridge() || method.isSynthetic())
            return false;

        final String name = method.getName();
        final Class<?> returnType = method.getReturnType();
        final boolean isGet = hasPrefix(name, GET) && returnType != void.class && !name.equals("getClass");
        final boolean isIs = hasPrefix(name, IS) && returnType == boolean.class;
        return isGet || isIs;
    }

    /**
     * Gets the name of the property represented by a getter, such as {@code name} for {@code getName()},
     * {@code active} for {@code isActive()} and {@code url} for {@code getURL()}
     * (the leading uppercase chars are converted to lowercase, as Jackson does).
     * @param getter the getter method
     * @return the property name
     */
    static String propertyName(final Method getter) {
        final String name = getter.getName();
        final String withoutPrefix = name.substring(name.startsWith(GET) ? GET.length() : IS.length());
        return decapitalize(withoutPrefix);
    }

    private static String decapitalize(final String name) {
        int upperCaseCount = 0;
        while (upperCaseCount < name.length() && Character.isUpperCase(name.charAt(upperCaseCount)))
            upperCaseCount++;

        return name.substring(0, upperCaseCount).toLowerCase(Locale.ROOT) + name.substring(upperCaseCount);
    }

    private static boolean hasPrefix(final String name, final String prefix) {
        return name.length() > prefix.length() && name.startsWith(prefix) && !Character.isLowerCase(name.charAt(prefix.length()));
    }
}
