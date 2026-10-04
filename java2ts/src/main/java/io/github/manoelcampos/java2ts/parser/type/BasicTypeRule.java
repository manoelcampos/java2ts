package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * Converts Java primitive types, their wrappers and other types that are serialized as
 * JSON primitive values (such as {@link String}, {@link java.math.BigDecimal} and {@link java.util.UUID}).
 * Check {@link BasicKind} for all the supported types.
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class BasicTypeRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link BasicTypeRule}.
     */
    public BasicTypeRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType()).flatMap(BasicKind::of).map(mapper.renderer()::basic);
    }
}
