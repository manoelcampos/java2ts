package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.parser.type.TypeMappingRule;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Replaces the type variables of the supertypes of a class by the type arguments the class gives them.
 * For instance, for {@code class PersonPage extends Page<Person>}, the inherited property {@code List<T> items}
 * gets the schema of {@code List<Person>}, since object schemas include all inherited properties.
 * Type variables that aren't bound (such as when a raw supertype is extended) accept any value,
 * except the ones declared by the class itself, which are parameters of the schema.
 * @author Manoel Campos
 */
final class TypeVariableBindings implements TypeMappingRule<SchemaType> {
    private final Class<?> aClass;
    private final Map<TypeVariable<?>, AnnotatedType> bindings = new HashMap<>();

    /**
     * Creates the bindings for the supertypes of a class.
     * @param aClass the class whose properties are converted
     */
    TypeVariableBindings(final Class<?> aClass) {
        this.aClass = aClass;
        bind(aClass);
    }

    private void bind(final Class<?> type) {
        Stream.concat(Stream.ofNullable(type.getAnnotatedSuperclass()), Stream.of(type.getAnnotatedInterfaces()))
              .forEach(this::bindSupertype);
    }

    private void bindSupertype(final AnnotatedType supertype) {
        switch (supertype.getType()) {
            case ParameterizedType parameterized when parameterized.getRawType() instanceof Class<?> raw -> {
                final AnnotatedType[] args = ((AnnotatedParameterizedType) supertype).getAnnotatedActualTypeArguments();
                final TypeVariable<?>[] variables = raw.getTypeParameters();
                for (int i = 0; i < variables.length; i++)
                    bindings.putIfAbsent(variables[i], args[i]);
                bind(raw);
            }
            case Class<?> raw -> bind(raw);
            default -> { /* other kinds of types can't be supertypes */ }
        }
    }

    @Override
    public Optional<SchemaType> map(final AnnotatedType type, final TypeMapper<SchemaType> mapper) {
        if (!(type.getType() instanceof TypeVariable<?> variable) || variable.getGenericDeclaration() == aClass)
            return Optional.empty();

        final AnnotatedType bound = bindings.get(variable);
        return Optional.of(bound == null ? mapper.renderer().any() : mapper.map(bound));
    }
}
