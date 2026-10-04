package io.github.manoelcampos.java2ts.fixtures;

/** A page of people, extending a generic class. */
public class PersonPage extends Page<Person> {
    private boolean lastPage;

    public boolean isLastPage() { return lastPage; }
}
