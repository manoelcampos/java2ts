package io.github.manoelcampos.java2ts.fixtures;

/** Uses an annotation with CLASS retention on record components, fields and getters. */
public class ClassRetainedModel {
    @ClassRetained
    private String fieldAnnotated;
    private String getterAnnotated;
    private String notAnnotated;

    public String getFieldAnnotated() { return fieldAnnotated; }

    @ClassRetained
    public String getGetterAnnotated() { return getterAnnotated; }

    public String getNotAnnotated() { return notAnnotated; }

    /** A record with a CLASS retention annotation. */
    public record Rec(@ClassRetained String annotated, String other) {}
}
