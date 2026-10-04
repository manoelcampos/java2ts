package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Converts date/time types ({@link Date}, {@link Calendar} and any {@link Temporal},
 * such as {@link java.time.LocalDate} and {@link java.time.LocalDateTime})
 * according to a {@link DateMapping}.
 * @author Manoel Campos
 */
public final class DateRule implements TypeMappingRule {
    private static final List<Class<?>> DATE_TYPES = List.of(Date.class, Calendar.class, Temporal.class);
    private final TsType dateType;

    /**
     * Creates a date rule.
     * @param mapping how date types are converted
     */
    public DateRule(final DateMapping mapping) {
        this.dateType = new TsBasicType(mapping.tsType());
    }

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(DateRule::isDate)
                             .map(dateClass -> dateType);
    }

    private static boolean isDate(final Class<?> aClass) {
        return DATE_TYPES.stream().anyMatch(dateClass -> dateClass.isAssignableFrom(aClass));
    }
}
