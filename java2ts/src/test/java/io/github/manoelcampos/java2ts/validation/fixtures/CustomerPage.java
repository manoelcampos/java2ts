package io.github.manoelcampos.java2ts.validation.fixtures;

import io.github.manoelcampos.java2ts.fixtures.Page;

/**
 * A page that binds the type variable of its superclass.
 */
public class CustomerPage extends Page<Customer> {
    /**
     * {@return the page number}
     */
    public int getNumber() { return 0; }
}
