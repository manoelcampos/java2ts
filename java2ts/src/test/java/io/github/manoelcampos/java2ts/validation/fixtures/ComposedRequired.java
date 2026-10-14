package io.github.manoelcampos.java2ts.validation.fixtures;

import io.github.manoelcampos.java2ts.fixtures.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Classes with composed constraints that contain required annotations (such as {@code @NotNull}).
 */
public final class ComposedRequired {
    private ComposedRequired() {/**/}

    /** A constraint composed of {@code @NotNull}. */
    @NotNull
    @Size(max = 5)
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface RequiredCode {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of {@code @NotBlank}. */
    @NotBlank
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface Text {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of another constraint that is composed of {@code @NotNull}. */
    @RequiredCode
    @Constraint(validatedBy = {})
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface NestedCode {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** An annotation that isn't a constraint, so its {@code @NotNull} isn't applied by Bean Validation. */
    @NotNull
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface NotConstraint {
    }

    /** A constraint with a validator that is also composed of {@code @NotNull}. */
    @NotNull
    @Constraint(validatedBy = AnyValidator.class)
    @Target({FIELD, RECORD_COMPONENT})
    @Retention(RUNTIME)
    public @interface Validated {
        String message() default "";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /** A constraint composed of itself, through {@link CycleEnd}, with no required annotation. */
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

    /** A validator that accepts any value. */
    public static final class AnyValidator implements ConstraintValidator<Validated, Object> {
        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            return true;
        }
    }

    /**
     * @param code a value with a constraint composed of {@code @NotNull}
     * @param text a value with a constraint composed of {@code @NotBlank}
     * @param nested a value with a nested composed constraint
     * @param plain a value with an annotation that isn't a constraint
     * @param validated a value with a constraint that has a validator
     * @param cycle a value with a constraint composed of itself
     */
    public record Values(
        @RequiredCode String code, @Text String text, @NestedCode String nested,
        @NotConstraint String plain, @Validated String validated, @CycleStart String cycle)
    {
    }

    /** @param code a nullable value with a constraint composed of {@code @NotNull} */
    public record NullableComposed(@Nullable @RequiredCode String code) {}

    /** @param code a nullable value with {@code @NotNull} directly on it */
    public record NullableDirect(@Nullable @NotNull String code) {}
}
