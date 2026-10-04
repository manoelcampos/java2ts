package io.github.manoelcampos.java2ts.javadoc;

import java.lang.reflect.AnnotatedElement;

/**
 * Provides the JavaDoc comments of classes and their members.
 * @author Manoel Campos
 */
public interface Javadoc {
    /** A Javadoc implementation that has no documentation at all, used when no JavaDoc is provided. */
    Javadoc NONE = new Javadoc() {
        @Override
        public String classComment(final Class<?> aClass) {
            return "";
        }

        @Override
        public String memberComment(final AnnotatedElement member) {
            return "";
        }
    };

    /**
     * {@return the documentation of a class, or an empty string if there is none}
     * @param aClass the class to get the documentation
     */
    String classComment(Class<?> aClass);

    /**
     * {@return the documentation of a class member, or an empty string if there is none}
     * For fields and methods, that is their own comment, while for record components,
     * that is the {@code @param} tag of the record.
     * @param member a field, method or record component
     */
    String memberComment(AnnotatedElement member);
}
