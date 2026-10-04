package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.ts.TsReferenceType;
import io.github.manoelcampos.java2ts.ts.TsType;

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
public final class DeclaredTypeRule implements TypeMappingRule {
    /**
     * Creates a {@link DeclaredTypeRule}.
     */
    public DeclaredTypeRule() {/**/}

    @Override
    public Optional<TsType> map(final AnnotatedType type, final TypeMapper mapper) {
        return AnnotatedTypes.rawClass(type.getType())
                             .filter(aClass -> !aClass.isArray())
                             .map(aClass -> reference(aClass, type, mapper));
    }

    private static TsType reference(final Class<?> aClass, final AnnotatedType type, final TypeMapper mapper) {
        mapper.context().discovery().accept(aClass);
        return new TsReferenceType(TsNames.of(aClass), typeArguments(aClass, type, mapper));
    }

    private static List<TsType> typeArguments(final Class<?> aClass, final AnnotatedType type, final TypeMapper mapper) {
        final List<AnnotatedType> args = AnnotatedTypes.typeArguments(type);
        if (args.isEmpty())
            return Collections.nCopies(aClass.getTypeParameters().length, TsType.ANY);

        return args.stream().map(mapper::map).toList();
    }
}
