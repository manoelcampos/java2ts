package io.github.manoelcampos.java2ts.fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A required annotation for tests (like jakarta.validation.constraints.NotNull). */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface Required {
}
