package io.github.manoelcampos.java2ts.validation.model;

import io.github.manoelcampos.java2ts.parser.type.MapKeyKind;

import static java.util.Objects.requireNonNull;

/**
 * The schema of a map, which is a JSON object with any keys.
 * @param keyKind the kind of the keys
 * @param key the schema of the keys (only meaningful for {@link MapKeyKind#ENUM} keys)
 * @param value the schema of the values
 * @author Manoel Campos
 */
public record RecordSchema(MapKeyKind keyKind, SchemaType key, SchemaType value) implements SchemaType {
    /**
     * Creates a {@link RecordSchema}, validating the components.
     */
    public RecordSchema {
        requireNonNull(keyKind);
        requireNonNull(key);
        requireNonNull(value);
    }

    @Override
    public String describe() {
        return "map";
    }
}
