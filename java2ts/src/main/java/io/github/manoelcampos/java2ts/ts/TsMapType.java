package io.github.manoelcampos.java2ts.ts;

import static java.util.Objects.requireNonNull;

/**
 * A TypeScript {@code Record} type, used to represent Java {@link java.util.Map}s,
 * such as {@code Record<string, number>}.
 * Maps whose keys are enums are partial records (such as {@code Partial<Record<Color, number>>}),
 * since the map doesn't need to have all enum constants as keys.
 *
 * @param keyType the type of the map keys
 * @param valueType the type of the map values
 * @param partial if not all keys are required to be present
 * @author Manoel Campos
 */
public record TsMapType(TsType keyType, TsType valueType, boolean partial) implements TsType {
    /**
     * Creates a {@link TsMapType}, validating the components and making immutable copies of collections.
     */
    public TsMapType {
        requireNonNull(keyType);
        requireNonNull(valueType);
    }

    @Override
    public String format() {
        final String record = "Record<%s, %s>".formatted(keyType.format(), valueType.format());
        return partial ? "Partial<%s>".formatted(record) : record;
    }
}
