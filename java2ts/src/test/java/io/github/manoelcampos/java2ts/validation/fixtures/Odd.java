package io.github.manoelcampos.java2ts.validation.fixtures;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes with constraints that can't be converted to schemas.
 */
public final class Odd {
    private Odd() {/**/}

    /** A custom constraint. */
    @Constraint(validatedBy = {})
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT})
    public @interface Even {
        String message() default "must be even";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** @param value a value with a custom constraint */
    public record CustomConstraint(@Even int value) {}

    /** @param value a number with a constraint for strings */
    public record EmailOnNumber(@Email Integer value) {}

    /** @param value a value of a class with a class-level constraint */
    @Even
    public record ClassConstraint(int value) {}

    /** @param value a string with a regular expression that JavaScript doesn't support */
    public record JavaOnlyRegex(@Pattern(regexp = "\\A[a-z]+") String value) {}

    /** @param value a string with a repeated constraint */
    public record Repeated(@Pattern(regexp = "[a-z]+") @Pattern(regexp = "a.*") String value) {}
}
