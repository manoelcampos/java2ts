package io.github.manoelcampos.java2ts.config;

/**
 * Defines how date/time types (such as {@link java.util.Date}, {@link java.time.LocalDate},
 * {@link java.time.LocalDateTime} and any other {@link java.time.temporal.Temporal}) are mapped to TypeScript.
 * @author Manoel Campos
 */
public enum DateMapping {
    /** Maps dates to the TypeScript {@code Date} type. */
    asDate("Date"),

    /** Maps dates to {@code string}, which is how they are usually serialized to JSON (ISO-8601). */
    asString("string"),

    /** Maps dates to {@code number}, for dates serialized as timestamps. */
    asNumber("number");

    private final String tsType;

    DateMapping(final String tsType) {
        this.tsType = tsType;
    }

    /**
     * {@return the name of the TypeScript type that dates are mapped to}
     */
    public String tsType() {
        return tsType;
    }
}
