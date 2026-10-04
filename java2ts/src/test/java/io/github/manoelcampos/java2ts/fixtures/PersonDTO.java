package io.github.manoelcampos.java2ts.fixtures;

import java.util.List;
import java.util.Map;

/** A DTO record implementing a generic interface. */
public record PersonDTO(
    @Nullable Long id, @Required String name, long countryId,
    Map<String, List<Map<Integer, Address>>> nested, @JsonIgnore String ignored) implements ModelRecord<Person>
{
    @Override
    public Person toModel() {
        return new Person();
    }
}
