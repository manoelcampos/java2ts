package io.github.manoelcampos.java2ts.validation.fixtures;

import io.github.manoelcampos.java2ts.fixtures.Required;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Past;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Date;
import java.util.OptionalInt;

/**
 * A record with all kinds of date/time types and other basic types.
 * @param time a time
 * @param offset a date-time with offset
 * @param year a year
 * @param legacy a legacy date
 * @param duration a duration
 * @param uri a URI
 * @param any any value
 * @param count an optional int
 * @param past a past date-time
 * @param future a future date-time
 */
public record Dates(
    @Required LocalTime time, @Required OffsetDateTime offset, @Required Year year, @Required Date legacy,
    @Required Duration duration, @Required URI uri, @Required Object any, OptionalInt count,
    @Required @Past LocalDateTime past, @Required @Future OffsetDateTime future)
{
}
