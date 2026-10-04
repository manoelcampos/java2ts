package io.github.manoelcampos.java2ts.fixtures;

import java.io.Serializable;

/**
 * Base interface for all models.
 * @author Manoel Campos
 */
public interface BaseModel extends Serializable {
    /** {@return the model id} */
    @Nullable
    Long getId();

    @JsonIgnore
    default boolean isNew() {
        return getId() == null;
    }
}
