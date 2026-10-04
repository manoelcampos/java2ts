package io.github.manoelcampos.java2ts.fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A nullable annotation for tests, which can be used on declarations and type usages. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE_USE, ElementType.RECORD_COMPONENT})
public @interface Nullable {
}
