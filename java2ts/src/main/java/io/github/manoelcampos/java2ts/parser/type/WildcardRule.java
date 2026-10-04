package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.Optional;

/**
 * Converts wildcard types: {@code ? extends T} is converted to {@code T},
 * while {@code ?} and {@code ? super T} are converted to {@code any}.
 * @author Manoel Campos
 */
public final class WildcardRule implements TypeMappingRule {
    /**
     * Creates a {@link WildcardRule}.
     */
    public WildcardRule() {/**/}

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        if (!(type instanceof AnnotatedWildcardType wildcard))
            return Optional.empty();

        if (wildcard.getAnnotatedLowerBounds().length > 0)
            return Optional.of(TsType.ANY);

        final AnnotatedType[] upperBounds = wildcard.getAnnotatedUpperBounds();
        return Optional.of(upperBounds.length == 0 ? TsType.ANY : mapper.map(upperBounds[0]));
    }
}
