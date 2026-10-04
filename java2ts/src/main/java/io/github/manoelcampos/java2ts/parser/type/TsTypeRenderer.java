package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.config.NullabilityDefinition;
import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsMapType;
import io.github.manoelcampos.java2ts.ts.TsNullableType;
import io.github.manoelcampos.java2ts.ts.TsArrayType;
import io.github.manoelcampos.java2ts.ts.TsReferenceType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.util.List;

/**
 * Creates TypeScript types for Java types.
 *
 * @param nullability how nullable types are written
 * @param dateMapping how date/time types are written
 * @author Manoel Campos
 */
public record TsTypeRenderer(NullabilityDefinition nullability, DateMapping dateMapping) implements TypeRenderer<TsType> {
    @Override
    public TsType any() {
        return TsType.ANY;
    }

    @Override
    public TsType custom(final String expression) {
        return new TsBasicType(expression);
    }

    @Override
    public TsType typeVariable(final String name) {
        return new TsReferenceType(name);
    }

    @Override
    public TsType basic(final BasicKind kind) {
        return switch (kind) {
            case ANY -> TsType.ANY;
            case VOID -> TsBasicType.VOID;
            case BOOLEAN -> TsBasicType.BOOLEAN;
            case INTEGER, DECIMAL -> TsBasicType.NUMBER;
            case CHAR, STRING, UUID, URI, URL, DURATION, PERIOD, ZONE_ID, BYTES -> TsBasicType.STRING;
        };
    }

    @Override
    public TsType date(final DateKind kind) {
        return new TsBasicType(dateMapping.tsType());
    }

    @Override
    public TsType array(final TsType element) {
        return new TsArrayType(element);
    }

    @Override
    public TsType map(final MapKeyKind keyKind, final TsType key, final TsType value) {
        return switch (keyKind) {
            case STRING -> new TsMapType(TsBasicType.STRING, value, false);
            case NUMBER -> new TsMapType(TsBasicType.NUMBER, value, false);
            case ENUM -> new TsMapType(key, value, true);
        };
    }

    @Override
    public TsType reference(final Class<?> aClass, final List<TsType> typeArguments) {
        return new TsReferenceType(TsNames.of(aClass), typeArguments);
    }

    @Override
    public TsType nullable(final TsType type) {
        return TsNullableType.of(type, nullability);
    }
}
