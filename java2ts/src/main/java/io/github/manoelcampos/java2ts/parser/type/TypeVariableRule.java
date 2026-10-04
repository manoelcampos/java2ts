package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsReferenceType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

/**
 * Converts generic type variables (such as {@code T}) to TypeScript type variables with the same name.
 * @author Manoel Campos
 */
public final class TypeVariableRule implements TypeMappingRule {
    /**
     * Creates a {@link TypeVariableRule}.
     */
    public TypeVariableRule() {/**/}

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return type.getType() instanceof TypeVariable<?> variable ?
                Optional.of(new TsReferenceType(variable.getName())) :
                Optional.empty();
    }
}
