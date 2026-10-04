package io.github.manoelcampos.java2ts.fixtures.selection;

public class Dog extends Animal {
    /** Nested classes are not matched by single star patterns. */
    public static class Puppy extends Dog {}
}
