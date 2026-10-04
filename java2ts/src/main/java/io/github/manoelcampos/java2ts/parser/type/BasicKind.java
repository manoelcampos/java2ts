package io.github.manoelcampos.java2ts.parser.type;

import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Kinds of Java types that are serialized as JSON primitive values, such as primitive types, their wrappers,
 * {@link String} and {@link UUID}. Each {@link TypeRenderer} defines how each kind is written,
 * so that the classification of Java types is defined in a single place.
 * @author Manoel Campos
 */
public enum BasicKind {
    /** {@link Object}, which can be any value. */
    ANY(Object.class),

    /** {@code void} and {@link Void}. */
    VOID(void.class, Void.class),

    /** {@code boolean} and {@link Boolean}. */
    BOOLEAN(boolean.class, Boolean.class),

    /** Integer numbers, such as {@code int}, {@link Long} and {@link BigInteger}. */
    INTEGER(byte.class, short.class, int.class, long.class, Byte.class, Short.class, Integer.class, Long.class,
            BigInteger.class, AtomicInteger.class, AtomicLong.class),

    /** Any other number, such as {@code double} and {@link java.math.BigDecimal}. */
    DECIMAL(float.class, double.class),

    /** {@code char} and {@link Character}, serialized as a string with a single character. */
    CHAR(char.class, Character.class),

    /** {@link String} and {@link CharSequence}. */
    STRING(String.class, CharSequence.class),

    /** {@link UUID}. */
    UUID(java.util.UUID.class),

    /** {@link URI}, which may be relative. */
    URI(java.net.URI.class),

    /** {@link URL}, which is always absolute. */
    URL(java.net.URL.class),

    /** {@link Duration}, serialized as an ISO-8601 duration. */
    DURATION(Duration.class),

    /** {@link Period}, serialized as an ISO-8601 period. */
    PERIOD(Period.class),

    /** {@link ZoneId}. */
    ZONE_ID(ZoneId.class),

    /** {@code byte[]}, serialized as a Base64 string. */
    BYTES(byte[].class);

    private static final Map<Class<?>, BasicKind> KINDS_BY_CLASS =
        Arrays.stream(values())
              .flatMap(kind -> kind.classes.stream().map(aClass -> Map.entry(aClass, kind)))
              .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private final List<Class<?>> classes;

    BasicKind(final Class<?>... classes) {
        this.classes = List.of(classes);
    }

    /**
     * {@return the kind of a class, or an empty Optional if it isn't a basic type}
     * Any subclass of {@link Number} not listed in {@link #INTEGER} is a {@link #DECIMAL}.
     * @param aClass the class to check
     */
    public static Optional<BasicKind> of(final Class<?> aClass) {
        return Optional.ofNullable(KINDS_BY_CLASS.get(aClass))
                       .or(() -> Number.class.isAssignableFrom(aClass) ? Optional.of(DECIMAL) : Optional.empty());
    }

    /**
     * {@return true if this kind is a number, false otherwise}
     */
    public boolean isNumber() {
        return this == INTEGER || this == DECIMAL;
    }
}
