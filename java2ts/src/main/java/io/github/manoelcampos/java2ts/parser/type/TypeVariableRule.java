package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

/**
 * Converts generic type variables (such as {@code T}) to TypeScript type variables with the same name.
 * @author Manoel Campos
 */
public final class TypeVariableRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link TypeVariableRule}.
     */
    public TypeVariableRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return type.getType() instanceof TypeVariable<?> variable ?
                Optional.of(mapper.renderer().typeVariable(variable.getName())) :
                Optional.empty();
    }
}
