package io.github.manoelcampos.java2ts.parser.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kinds of date/time types ({@link Date}, {@link Calendar} and any {@link Temporal}).
 * Each {@link TypeRenderer} defines how each kind is written (for instance, according to a
 * {@link io.github.manoelcampos.java2ts.config.DateMapping}).
 * @author Manoel Campos
 */
public enum DateKind {
    /** A date without time, such as {@link LocalDate} ({@code 2026-10-04}). */
    LOCAL_DATE,

    /** A date and time without offset, such as {@link LocalDateTime} ({@code 2026-10-04T10:15:30}). */
    LOCAL_DATE_TIME,

    /** A time without date, such as {@link LocalTime} ({@code 10:15:30}). */
    LOCAL_TIME,

    /** An instant in UTC, such as {@link Instant} ({@code 2026-10-04T13:15:30Z}). */
    INSTANT,

    /** A date and time with offset, such as {@link OffsetDateTime}, {@link ZonedDateTime},
     * {@link Date} and {@link Calendar} ({@code 2026-10-04T10:15:30-03:00}). */
    OFFSET_DATE_TIME,

    /** Any other date/time type, such as {@link java.time.Year} and {@link java.time.YearMonth}. */
    OTHER;

    private static final String SQL_DATE = "java.sql.Date";
    private static final String SQL_TIME = "java.sql.Time";
    private static final List<Class<?>> DATE_TYPES = List.of(Date.class, Calendar.class, Temporal.class);
    private static final Map<Class<?>, DateKind> KINDS_BY_CLASS = Map.of(
        LocalDate.class, LOCAL_DATE,
        LocalDateTime.class, LOCAL_DATE_TIME,
        LocalTime.class, LOCAL_TIME,
        Instant.class, INSTANT,
        OffsetDateTime.class, OFFSET_DATE_TIME,
        ZonedDateTime.class, OFFSET_DATE_TIME
    );

    /**
     * {@return the kind of a class, or an empty Optional if it isn't a date/time type}
     * @param aClass the class to check
     */
    public static Optional<DateKind> of(final Class<?> aClass) {
        if (DATE_TYPES.stream().noneMatch(dateClass -> dateClass.isAssignableFrom(aClass)))
            return Optional.empty();

        return Optional.of(switch (aClass.getName()) {
            case SQL_DATE -> LOCAL_DATE;
            case SQL_TIME -> LOCAL_TIME;
            default -> legacyOrTemporal(aClass);
        });
    }

    private static DateKind legacyOrTemporal(final Class<?> aClass) {
        if (Date.class.isAssignableFrom(aClass) || Calendar.class.isAssignableFrom(aClass))
            return OFFSET_DATE_TIME;

        return KINDS_BY_CLASS.getOrDefault(aClass, OTHER);
    }
}
