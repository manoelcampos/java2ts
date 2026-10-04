package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.util.Optional;

/**
 * Converts types that must not be declared in TypeScript to {@code any}.
 * They are JDK types not handled by previous rules (such as {@link java.io.Serializable})
 * and types excluded by the user.
 * @author Manoel Campos
 */
public final class UndeclarableTypeRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link UndeclarableTypeRule}.
     */
    public UndeclarableTypeRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(aClass -> JdkTypes.isJdkType(aClass) || mapper.context().exclusion().test(aClass))
                             .map(aClass -> mapper.renderer().any());
    }
}
