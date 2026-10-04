package io.github.manoelcampos.java2ts.fixtures;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Marks entity classes, used to test the selection of classes by annotation. */
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
}
