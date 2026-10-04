package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Map.entry;

/**
 * Converts Java primitive types, their wrappers and other types that are serialized as
 * JSON primitive values (such as {@link String}, {@link java.math.BigDecimal} and {@link UUID}).
 * Any subclass of {@link Number} is converted to {@code number}.
 * @author Manoel Campos
 */
public final class BasicTypeRule implements TypeMappingRule {
    /**
     * Creates a {@link BasicTypeRule}.
     */
    public BasicTypeRule() {/**/}

    private static final Map<Class<?>, TsType> TYPES = Map.ofEntries(
        entry(Object.class, TsType.ANY),
        entry(void.class, TsBasicType.VOID),
        entry(Void.class, TsBasicType.VOID),
        entry(boolean.class, TsBasicType.BOOLEAN),
        entry(Boolean.class, TsBasicType.BOOLEAN),
        entry(byte.class, TsBasicType.NUMBER),
        entry(short.class, TsBasicType.NUMBER),
        entry(int.class, TsBasicType.NUMBER),
        entry(long.class, TsBasicType.NUMBER),
        entry(float.class, TsBasicType.NUMBER),
        entry(double.class, TsBasicType.NUMBER),
        entry(char.class, TsBasicType.STRING),
        entry(Character.class, TsBasicType.STRING),
        entry(String.class, TsBasicType.STRING),
        entry(CharSequence.class, TsBasicType.STRING),
        entry(UUID.class, TsBasicType.STRING),
        entry(URI.class, TsBasicType.STRING),
        entry(URL.class, TsBasicType.STRING),
        entry(Duration.class, TsBasicType.STRING),
        entry(Period.class, TsBasicType.STRING),
        entry(ZoneId.class, TsBasicType.STRING),
        entry(byte[].class, TsBasicType.STRING)
    );

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType()).flatMap(BasicTypeRule::map);
    }

    private static Optional<TsType> map(final Class<?> aClass) {
        if (Number.class.isAssignableFrom(aClass))
            return Optional.of(TsBasicType.NUMBER);

        return Optional.ofNullable(TYPES.get(aClass));
    }
}
