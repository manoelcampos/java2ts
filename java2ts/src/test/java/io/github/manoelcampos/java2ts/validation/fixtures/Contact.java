package io.github.manoelcampos.java2ts.validation.fixtures;

import io.github.manoelcampos.java2ts.fixtures.Address;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * A Java Bean whose getter doesn't have the type annotations of its field (as Lombok getters).
 */
public class Contact {
    private List<@NotBlank String> phones;
    private Address address;

    /**
     * {@return the phones}
     */
    public List<String> getPhones() { return phones; }

    /**
     * {@return the address, whose schema is written by hand}
     */
    public Address getAddress() { return address; }
}
