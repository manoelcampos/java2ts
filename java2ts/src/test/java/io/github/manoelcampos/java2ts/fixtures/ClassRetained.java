package io.github.manoelcampos.java2ts.fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** An annotation not available via reflection (like lombok.NonNull), which is read from class files. */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE_USE})
public @interface ClassRetained {
}
