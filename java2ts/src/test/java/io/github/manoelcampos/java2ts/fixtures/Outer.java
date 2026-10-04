package io.github.manoelcampos.java2ts.fixtures;

/** A class with a nested class. */
public class Outer {
    private Inner inner;

    public Inner getInner() { return inner; }

    /** A nested class. */
    public static class Inner {
        /** A public field. */
        public int value;
    }
}
