package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsArrayType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * Converts Java arrays (such as {@code String[]} or {@code T[][]}) to TypeScript arrays.
 * {@code byte[]} is handled by the {@link BasicTypeRule}, since it's serialized as a Base64 string.
 * @author Manoel Campos
 */
public final class ArrayRule implements TypeMappingRule {
    /**
     * Creates a {@link ArrayRule}.
     */
    public ArrayRule() {/**/}

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return type instanceof AnnotatedArrayType array ?
                Optional.of(new TsArrayType(mapper.map(array.getAnnotatedGenericComponentType()))) :
                Optional.empty();
    }
}
