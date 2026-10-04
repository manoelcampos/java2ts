package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.Optional;

/**
 * Converts wildcard types: {@code ? extends T} is converted to {@code T},
 * while {@code ?} and {@code ? super T} are converted to {@code any}.
 * @author Manoel Campos
 */
public final class WildcardRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link WildcardRule}.
     */
    public WildcardRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        if (!(type instanceof AnnotatedWildcardType wildcard))
            return Optional.empty();

        if (wildcard.getAnnotatedLowerBounds().length > 0)
            return Optional.of(mapper.renderer().any());

        final AnnotatedType[] upperBounds = wildcard.getAnnotatedUpperBounds();
        return Optional.of(upperBounds.length == 0 ? mapper.renderer().any() : mapper.map(upperBounds[0]));
    }
}
