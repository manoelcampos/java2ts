package io.github.manoelcampos.java2ts.sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * Base interface for all models.
 * {@link Serializable} is ignored when generating TypeScript, since it's not useful for the frontend.
 */
public interface BaseModel extends Serializable {
    /**
     * {@return the model id, which is null for objects not saved yet}
     */
    @Nullable
    Long getId();

    /** Ignored by Jackson, so it's not included in the TypeScript interface. */
    @JsonIgnore
    default boolean isNew() {
        return getId() == null;
    }
}
