package io.github.manoelcampos.java2ts.parser.type;


import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Converts any other class (such as POJOs, records, enums and interfaces) to a reference
 * to a TypeScript declaration with the class name, including generic type arguments.
 * The class is reported to the {@link TypeContext#discovery()}, so that it's declared too.
 * Raw usages of generic classes get {@code any} as type arguments, since TypeScript requires them.
 * @author Manoel Campos
 */
public final class DeclaredTypeRule<R> implements TypeMappingRule<R> {
    /**
     * Creates a {@link DeclaredTypeRule}.
     */
    public DeclaredTypeRule() {/**/}

    @Override
    public Optional<R> map(final AnnotatedType type, final TypeMapper<R> mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(aClass -> !aClass.isArray())
                             .map(aClass -> reference(aClass, type, mapper));
    }

    private static <R> R reference(final Class<?> aClass, final AnnotatedType type, final TypeMapper<R> mapper) {
        mapper.context().discovery().accept(aClass);
        return mapper.renderer().reference(aClass, typeArguments(aClass, type, mapper));
    }

    private static <R> List<R> typeArguments(final Class<?> aClass, final AnnotatedType type, final TypeMapper<R> mapper) {
        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        if (args.isEmpty())
            return Collections.nCopies(aClass.getTypeParameters().length, mapper.renderer().any());

        return args.stream().map(mapper::map).toList();
    }
}
