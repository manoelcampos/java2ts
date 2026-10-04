package io.github.manoelcampos.java2ts.fixtures;

/** The status of a person. */
public enum Status {
    ACTIVE,
    INACTIVE,
    @JsonProperty("removed")
    DELETED;

    /** Enum fields are not converted, since enums are serialized by their names. */
    private final String label = name().toLowerCase();

    public String getLabel() {
        return label;
    }
}
