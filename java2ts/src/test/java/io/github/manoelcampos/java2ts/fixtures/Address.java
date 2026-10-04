package io.github.manoelcampos.java2ts.fixtures;

import java.util.List;

/**
 * An address.
 * @param street the street name
 * @param number the address number
 * @param complements optional complements
 */
public record Address(@Required String street, @Nullable Integer number, List<@Nullable String> complements) {
}
