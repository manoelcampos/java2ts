package io.github.manoelcampos.java2ts.fixtures.selection;

/** Used to test the selection of classes by supertypes. */
public abstract class Animal implements Comparable<Animal> {
    @Override
    public int compareTo(final Animal other) {
        return 0;
    }
}
