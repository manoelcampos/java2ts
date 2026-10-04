package io.github.manoelcampos.java2ts.sample.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * An error returned by a REST API.
 * Its fields are final, so they become read-only TypeScript properties.
 */
public final class ApiError {
    /** The HTTP status code. */
    private final int status;

    /** A message describing the error. */
    @NotNull
    private final String message;

    /** When the error happened. */
    @NotNull
    private final LocalDateTime timestamp;

    /**
     * Creates an API error.
     * @param status the HTTP status code
     * @param message a message describing the error
     * @param timestamp when the error happened
     */
    public ApiError(final int status, final String message, final LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
