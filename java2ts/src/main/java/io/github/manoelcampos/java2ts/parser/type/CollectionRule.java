package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;

/**
 * Converts any {@link Iterable} (such as {@link List}, {@link java.util.Set} and other {@link java.util.Collection}s)
 * to a TypeScript array, such as {@code List<String>} to {@code string[]}.
 * Raw collections are converted to {@code any[]}.
 * @author Manoel Campos
 */
public final class CollectionRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link CollectionRule}.
     */
    public CollectionRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(Iterable.class::isAssignableFrom)
                             .map(collectionClass -> mapper.renderer().array(elementType(type, mapper)));
    }

    private static <R> R elementType(final AnnotatedType type, final TypeMapper<R> mapper) {
        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        return args.size() == 1 ? mapper.map(args.getFirst()) : mapper.renderer().any();
    }
}
