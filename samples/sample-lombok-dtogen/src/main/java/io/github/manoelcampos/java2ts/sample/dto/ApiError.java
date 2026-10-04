package io.github.manoelcampos.java2ts.sample.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * An error returned by a REST API.
 * Lombok's {@code @Value} makes all fields private and final, so they become read-only TypeScript properties.
 */
@Value
public class ApiError {
    /** The HTTP status code. */
    int status;

    /** A message describing the error. */
    @NotNull
    String message;

    /** When the error happened. */
    @NotNull
    LocalDateTime timestamp;
}
