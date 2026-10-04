package io.github.manoelcampos.java2ts.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utility methods to find fields of classes.
 * @author Manoel Campos
 */
final class Members {
    private Members() {/**/}

    /**
     * {@return the non-static fields of a class and its superclasses, starting from the topmost superclass}
     * @param aClass the class to get the fields
     */
    static List<Field> instanceFields(final Class<?> aClass) {
        final var fields = new ArrayList<Field>();
        for (Class<?> current = aClass; current != null && current != Object.class; current = current.getSuperclass()) {
            final List<Field> declared = Arrays.stream(current.getDeclaredFields())
                                               .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
                                               .toList();
            fields.addAll(0, declared);
        }

        return fields;
    }

    /**
     * {@return an Optional with a field declared directly in a class, or an empty Optional if it doesn't exist}
     * @param aClass the class to look for the field
     * @param name the field name
     */
    static Optional<Field> declaredField(final Class<?> aClass, final String name) {
        return Arrays.stream(aClass.getDeclaredFields()).filter(field -> field.getName().equals(name)).findFirst();
    }
}
