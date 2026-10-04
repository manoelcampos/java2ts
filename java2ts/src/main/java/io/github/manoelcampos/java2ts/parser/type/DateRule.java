package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * Converts date/time types ({@link java.util.Date}, {@link java.util.Calendar} and any {@link java.time.temporal.Temporal},
 * such as {@link java.time.LocalDate} and {@link java.time.LocalDateTime}).
 * Check {@link DateKind} for how they are classified.
 * @param <R> the type of the result (such as a TypeScript type)
 * @author Manoel Campos
 */
public final class DateRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link DateRule}.
     */
    public DateRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType()).flatMap(DateKind::of).map(mapper.renderer()::date);
    }
}
