package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * Converts Java arrays (such as {@code String[]} or {@code T[][]}) to TypeScript arrays.
 * {@code byte[]} is handled by the {@link BasicTypeRule}, since it's serialized as a Base64 string.
 * @author Manoel Campos
 */
public final class ArrayRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link ArrayRule}.
     */
    public ArrayRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return type instanceof AnnotatedArrayType array ?
                Optional.of(mapper.renderer().array(mapper.map(array.getAnnotatedGenericComponentType()))) :
                Optional.empty();
    }
}
