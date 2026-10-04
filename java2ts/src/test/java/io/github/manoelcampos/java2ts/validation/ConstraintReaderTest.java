package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.validation.model.Constraint;
import io.github.manoelcampos.java2ts.validation.model.LengthConstraint;
import io.github.manoelcampos.java2ts.validation.model.NotBlankConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintReaderTest {
    private final ConstraintReader reader = new ConstraintReader();

    @NotNull @NotBlank @Size(min = 2, max = 10, message = "between {min} and {max}")
    private String interpolated = "";

    @Size(message = "{jakarta.validation.constraints.Size.message}")
    private String template = "";

    @Size(message = "invalid ${validatedValue}")
    private String expression = "";

    @Deprecated
    private String notConstraint = "";

    private List<Constraint> read(final String fieldName, final Class<? extends Annotation> annotationType) throws NoSuchFieldException {
        return reader.read(getClass().getDeclaredField(fieldName).getAnnotation(annotationType));
    }

    @Test
    void interpolatesAttributesInMessages() throws NoSuchFieldException {
        final var expected = new LengthConstraint(OptionalInt.of(2), OptionalInt.of(10), Optional.of("between 2 and 10"));
        assertEquals(List.of(expected), read("interpolated", Size.class));
    }

    @Test
    void ignoresTemplatesAndExpressionsInMessages() throws NoSuchFieldException {
        final var expected = new LengthConstraint(OptionalInt.empty(), OptionalInt.empty(), Optional.empty());
        assertEquals(List.of(expected), read("template", Size.class));
        assertEquals(List.of(expected), read("expression", Size.class));
    }

    @Test
    void readsNotBlankAndIgnoresNotNull() throws NoSuchFieldException {
        assertEquals(List.of(new NotBlankConstraint(Optional.empty())), read("interpolated", NotBlank.class));
        assertTrue(read("interpolated", NotNull.class).isEmpty());
    }

    @Test
    void ignoresAnnotationsThatAreNotConstraints() throws NoSuchFieldException {
        assertTrue(read("notConstraint", Deprecated.class).isEmpty());
    }
}
