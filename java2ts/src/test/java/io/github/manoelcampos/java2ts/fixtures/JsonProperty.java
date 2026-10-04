package io.github.manoelcampos.java2ts.fixtures;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Simulates the Jackson annotation with the same name, which is identified by its simple name. */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {
    String value() default "";
}
