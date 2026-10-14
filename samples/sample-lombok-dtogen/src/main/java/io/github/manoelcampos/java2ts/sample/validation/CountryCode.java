package io.github.manoelcampos.java2ts.sample.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A custom constraint composed of other constraints, which java2ts converts to their schemas:
 * an ISO 3166-1 alpha-2 country code, such as BR.
 * Since it's composed of {@code @NotNull}, the annotated properties are required.
 */
@Documented
@NotNull
@Pattern(regexp = CountryCode.REGEX)
@Size(min = CountryCode.LENGTH, max = CountryCode.LENGTH)
@Constraint(validatedBy = {})
@Target({FIELD, METHOD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
public @interface CountryCode {
    /** The format of the code: two uppercase letters. */
    String REGEX = "[A-Z]{2}";

    /** The number of letters of the code. */
    int LENGTH = 2;

    String message() default "invalid country code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
