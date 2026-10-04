package io.github.manoelcampos.java2ts.validation.model;

/**
 * Indicates a Java type or validation constraint can't be converted to a schema,
 * which fails the generation so that backend and frontend validations can't drift.
 * @author Manoel Campos
 */
public class UnsupportedValidationException extends RuntimeException {
    /**
     * Creates the exception.
     * @param message the reason the type or constraint can't be converted
     */
    public UnsupportedValidationException(final String message) {
        super(message);
    }

    /**
     * {@return a new exception with the same message, prefixed with where the problem was found}
     * @param location where the problem was found (such as a class and property name)
     */
    public UnsupportedValidationException at(final String location) {
        return new UnsupportedValidationException(location + ": " + getMessage());
    }
}
