package io.github.manoelcampos.java2ts.validation.fixtures;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Classes with composed constraints: custom constraints made of other constraints.
 */
public final class Composed {
    private Composed() {/**/}

    /** A constraint composed of standard constraints. */
    @Documented
    @Pattern(regexp = CityCode.REGEX)
    @Size(max = 7)
    @Constraint(validatedBy = {})
    @Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface CityCode {
        /** The format of the code. */
        String REGEX = "[0-9]{7}";
        String message() default "invalid city code";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of another composed constraint and a container of repeated constraints. */
    @NotBlank
    @CityCode
    @Pattern.List({@Pattern(regexp = "1.*"), @Pattern(regexp = ".*9")})
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface NorthCityCode {
        String message() default "invalid north city code";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A composed constraint that reports a single violation with its own message. */
    @CityCode
    @Size(min = 7)
    @ReportAsSingleViolation
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface SingleViolation {
        String message() default "must have {length} digits";
        int length() default 7;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint with a validator, whose code can't be converted. */
    @Constraint(validatedBy = EvenValidator.class)
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface WithValidator {
        String message() default "must be even";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint with a validator and composing constraints. */
    @Size(max = 10)
    @Constraint(validatedBy = EvenValidator.class)
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface ComposedWithValidator {
        String message() default "must be even";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A composed constraint that overrides attributes of its composing constraints. */
    @NotBlank
    @Size
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface RequiredText {
        @OverridesAttribute(constraint = Size.class, name = "max")
        int max() default Integer.MAX_VALUE;

        /** Overrides the attribute with the same name, since the {@code name} isn't set. */
        @OverridesAttribute(constraint = Size.class)
        String message() default "up to {max} characters";

        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A composed constraint that overrides attributes of a composed constraint and of repeated constraints. */
    @RequiredText
    @Pattern.List({@Pattern(regexp = "a.*"), @Pattern(regexp = ".*z")})
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface Name {
        @OverridesAttribute(constraint = RequiredText.class, name = "max")
        int length() default 30;

        @OverridesAttribute.List({
            @OverridesAttribute(constraint = Pattern.class, name = "regexp", constraintIndex = 0),
            @OverridesAttribute(constraint = Pattern.class, name = "regexp", constraintIndex = 1)
        })
        String format() default "[a-z]+";

        String message() default "invalid name";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A composed constraint that overrides an attribute of a constraint it isn't composed of. */
    @Size
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface WrongOverride {
        @OverridesAttribute(constraint = Size.class, name = "max", constraintIndex = 1)
        int max() default 1;

        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of itself, through {@link CycleEnd}. */
    @CycleEnd
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface CycleStart {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of {@link CycleStart}, which is composed of this one. */
    @CycleStart
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface CycleEnd {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A validator that checks if a number is even. */
    public static final class EvenValidator implements ConstraintValidator<WithValidator, Integer> {
        @Override
        public boolean isValid(final Integer value, final ConstraintValidatorContext context) {
            return value % 2 == 0;
        }
    }

    /** A constraint composed of {@code @NotNull} and of another composed constraint. */
    @NotNull
    @CityCode
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface RequiredCityCode {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** @param value a string with a composed constraint */
    public record City(@CityCode String value) {}

    /** @param value a string with a composed constraint that makes it required */
    public record RequiredCity(@RequiredCityCode String value) {}

    /** @param value a string with a nested composed constraint */
    public record NorthCity(@NorthCityCode String value) {}

    /** @param value a string with a composed constraint that reports a single violation */
    public record Single(@SingleViolation String value) {}

    /** @param value a number with a constraint that has a validator */
    public record Validated(@WithValidator Integer value) {}

    /** @param value a string with a constraint that has a validator and composing constraints */
    public record ValidatedComposed(@ComposedWithValidator String value) {}

    /** @param value a string with a constraint that overrides attributes */
    public record Overriding(@RequiredText(max = 60) String value) {}

    /** @param value a string with a constraint that overrides attributes of a composed constraint and of repeated constraints */
    public record NestedOverriding(@Name(length = 20, format = "[a-z]{2,}") String value) {}

    /** @param value a string with a constraint that overrides an attribute of a constraint it isn't composed of */
    public record WrongOverriding(@WrongOverride String value) {}

    /** @param value a string with a constraint composed of itself */
    public record Cycle(@CycleStart String value) {}
}
