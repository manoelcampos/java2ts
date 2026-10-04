package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.parser.type.AnnotatedTypes;
import io.github.manoelcampos.java2ts.parser.type.TsNames;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.ts.TsDeclaration;
import io.github.manoelcampos.java2ts.ts.TsInterface;
import io.github.manoelcampos.java2ts.ts.TsProperty;
import io.github.manoelcampos.java2ts.ts.TsReferenceType;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts Java classes, records and interfaces to TypeScript interfaces.
 *
 * <p>The TypeScript interface extends the interfaces generated for the Java supertypes.
 * Supertypes that aren't declared in TypeScript (such as JDK types like {@link java.io.Serializable}
 * and {@link Comparable}) are ignored. The interface declares just the properties that are not
 * inherited from the declared supertypes.</p>
 * @author Manoel Campos
 */
public final class InterfaceDeclarationParser implements DeclarationParser {
    private final TypeMapper typeMapper;
    private final PropertyResolver propertyResolver;
    private final Javadoc javadoc;

    /**
     * Creates an interface parser.
     * @param typeMapper the mapper to convert types
     * @param propertyResolver the resolver to convert properties
     * @param javadoc where to get documentation from
     */
    public InterfaceDeclarationParser(final TypeMapper typeMapper, final PropertyResolver propertyResolver, final Javadoc javadoc) {
        this.typeMapper = typeMapper;
        this.propertyResolver = propertyResolver;
        this.javadoc = javadoc;
    }

    @Override
    public TsDeclaration parse(final Class<?> aClass) {
        final List<DeclaredSupertype> supertypes = declaredSupertypes(aClass);
        final Set<String> inheritedNames = supertypes.stream()
                                                     .flatMap(supertype -> PropertyExtractor.propertiesOf(supertype.rawClass()).stream())
                                                     .map(JavaProperty::name)
                                                     .collect(Collectors.toSet());

        final List<TsProperty> properties = PropertyExtractor.propertiesOf(aClass).stream()
                                                             .filter(property -> !inheritedNames.contains(property.name()))
                                                             .map(propertyResolver::resolve)
                                                             .toList();

        final List<String> typeParams = Arrays.stream(aClass.getTypeParameters()).map(this::typeParameter).toList();
        final List<TsType> superTsTypes = supertypes.stream().map(DeclaredSupertype::type).toList();
        final String comment = Deprecation.addTag(javadoc.classComment(aClass), List.of(aClass));
        return new TsInterface(TsNames.of(aClass), typeParams, superTsTypes, properties, comment);
    }

    /**
     * {@return a TypeScript type parameter declaration, including its bounds}
     * For instance, {@code T extends Number & Serializable} is converted to {@code T extends number},
     * since bounds converted to {@code any} (such as JDK interfaces) are ignored.
     * Multiple bounds are converted to an intersection type, such as {@code T extends Animal & Pet}.
     */
    private String typeParameter(final TypeVariable<?> variable) {
        final String bounds = Arrays.stream(variable.getAnnotatedBounds())
                                    .map(typeMapper::map)
                                    .filter(bound -> !TsType.ANY.equals(bound))
                                    .map(TsType::format)
                                    .collect(Collectors.joining(" & "));

        return bounds.isEmpty() ? variable.getName() : "%s extends %s".formatted(variable.getName(), bounds);
    }

    /**
     * {@return the direct supertypes of a class which are declared in TypeScript}
     * Only supertypes converted to references to TypeScript declarations are kept
     * (JDK and excluded types are converted to {@code any}, collections are converted to arrays, etc.).
     */
    private List<DeclaredSupertype> declaredSupertypes(final Class<?> aClass) {
        return Stream.concat(Stream.ofNullable(aClass.getAnnotatedSuperclass()), Arrays.stream(aClass.getAnnotatedInterfaces()))
                     .map(this::toDeclaredSupertype)
                     .flatMap(Optional::stream)
                     .toList();
    }

    private Optional<DeclaredSupertype> toDeclaredSupertype(final AnnotatedType supertype) {
        final Optional<Class<?>> rawClass = AnnotatedTypes.rawClass(supertype.getType());
        return rawClass.flatMap(raw -> typeMapper.map(supertype) instanceof TsReferenceType reference ?
                                    Optional.of(new DeclaredSupertype(raw, reference)) :
                                    Optional.empty());
    }

    /**
     * A Java supertype that is declared in TypeScript.
     * @param rawClass the supertype class
     * @param type the TypeScript type referencing the supertype declaration
     */
    private record DeclaredSupertype(Class<?> rawClass, TsType type) {}
}
