package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsArrayType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;

/**
 * Converts any {@link Iterable} (such as {@link List}, {@link java.util.Set} and other {@link java.util.Collection}s)
 * to a TypeScript array, such as {@code List<String>} to {@code string[]}.
 * Raw collections are converted to {@code any[]}.
 * @author Manoel Campos
 */
public final class CollectionRule implements TypeMappingRule {
    /**
     * Creates a {@link CollectionRule}.
     */
    public CollectionRule() {/**/}

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(Iterable.class::isAssignableFrom)
                             .map(collectionClass -> new TsArrayType(elementType(type, mapper)));
    }

    private static TsType elementType(final AnnotatedType type, final TypeMapper mapper) {
        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        return args.size() == 1 ? mapper.map(args.getFirst()) : TsType.ANY;
    }
}
