package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.config.OptionalPropertiesDeclaration;
import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.parser.type.AnnotatedTypes;
import io.github.manoelcampos.java2ts.parser.type.OptionalRule;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.ts.TsNullableType;
import io.github.manoelcampos.java2ts.ts.TsProperty;
import io.github.manoelcampos.java2ts.ts.TsType;
import io.github.manoelcampos.java2ts.ts.TsUnionType;

import java.util.Set;

/**
 * Converts Java properties to TypeScript ones, defining their types, nullability, optionality and documentation.
 * <ul>
 *   <li>A property is nullable when it has any of the {@link Settings#nullableAnnotations()}.</li>
 *   <li>A property is read-only when it's a record component or its field is final,
 *   and {@link Settings#readonlyProperties()} is enabled.</li>
 *   <li>A property is optional when its type is a Java {@link java.util.Optional}, or when
 *   {@link Settings#requiredAnnotations()} is not empty and the property has none of them,
 *   either directly or inside a composed Bean Validation constraint
 *   (primitive properties are never optional, since they can't be null).
 *   Optional properties are declared according to the {@link Settings#optionalPropertiesDeclaration()}.</li>
 * </ul>
 * @author Manoel Campos
 */
public final class PropertyResolver {
    private static final String UNDEFINED = "undefined";
    private final Settings settings;
    private final TypeMapper<TsType> typeMapper;
    private final Javadoc javadoc;

    /**
     * Creates a property resolver.
     * @param settings the conversion settings
     * @param typeMapper the mapper to convert property types
     * @param javadoc where to get property documentation from
     */
    public PropertyResolver(final Settings settings, final TypeMapper<TsType> typeMapper, final Javadoc javadoc) {
        this.settings = settings;
        this.typeMapper = typeMapper;
        this.javadoc = javadoc;
    }

    /**
     * {@return the TypeScript property for a Java property}
     * @param property the Java property to convert
     */
    public TsProperty resolve(final JavaProperty property) {
        checkNullableComposedRequired(property);
        final boolean javaOptional = isJavaOptional(property);
        final TsType type = baseType(property, javaOptional);
        final boolean optional = javaOptional || isOptionalByAnnotations(property);
        final String comment = comment(property);
        final boolean readonly = settings.readonlyProperties() && property.isReadonly();
        if (!optional)
            return new TsProperty(property.name(), type, false, readonly, comment);

        final OptionalPropertiesDeclaration declaration = settings.optionalPropertiesDeclaration();
        final TsType optionalType = TsUnionType.combine(type, declaration.extraTypes());
        final boolean questionMark = declaration.usesQuestionMark();
        return new TsProperty(property.name(), questionMark ? withoutUndefined(optionalType) : optionalType, questionMark, readonly, comment);
    }

    /**
     * Checks if a nullable property has a composed constraint that makes it required,
     * since it's not clear which one must be used.
     * @throws IllegalStateException if the property is nullable and has such a constraint
     */
    private void checkNullableComposedRequired(final JavaProperty property) {
        if (!property.hasAnyAnnotation(settings.nullableAnnotations()))
            return;

        property.findConstraintComposedOfAny(settings.requiredAnnotations()).ifPresent(constraint -> {
            final var msg = "%s: the property has a nullable annotation, but its constraint @%s is composed of a required annotation. Remove one of them";
            throw new IllegalStateException(msg.formatted(property.qualifiedName(), constraint.annotationType().getSimpleName()));
        });
    }

    /**
     * {@return the property type, which is nullable if the property has any nullable annotation}
     * For Java Optional properties, the type is the Optional value type, since the property becomes optional.
     */
    private TsType baseType(final JavaProperty property, final boolean javaOptional) {
        final TsType mapped = typeMapper.map(property.type());
        if (javaOptional && mapped instanceof TsNullableType nullable)
            return nullable.type();

        return property.hasAnyAnnotation(settings.nullableAnnotations()) ? typeMapper.renderer().nullable(mapped) : mapped;
    }

    private static boolean isJavaOptional(final JavaProperty property) {
        return AnnotatedTypes.rawClass(property.type().getType()).filter(OptionalRule::isOptional).isPresent();
    }

    /**
     * {@return true if the property is optional because it doesn't have any of the required annotations}
     * Primitive properties are always required, since they can't be null.
     */
    private boolean isOptionalByAnnotations(final JavaProperty property) {
        final boolean primitive = property.type().getType() instanceof Class<?> aClass && aClass.isPrimitive();
        return !primitive && !settings.requiredAnnotations().isEmpty() && !isRequiredByAnnotations(property);
    }

    /**
     * {@return true if the property has any of the required annotations}, directly or inside a composed constraint
     */
    private boolean isRequiredByAnnotations(final JavaProperty property) {
        final Set<String> required = settings.requiredAnnotations();
        return property.hasAnyAnnotation(required) || property.findConstraintComposedOfAny(required).isPresent();
    }

    /**
     * {@return a type without undefined}, since a property declared with a question mark already accepts undefined.
     */
    private static TsType withoutUndefined(final TsType type) {
        final TsType flat = TsUnionType.of(type.unionMembers());
        return flat instanceof TsUnionType union ? union.without(UNDEFINED) : flat;
    }

    /**
     * {@return the first non-blank documentation from the elements defining the property,
     * including the {@code @deprecated} tag when the property is deprecated}
     */
    private String comment(final JavaProperty property) {
        final String comment = property.elements().stream()
                                       .map(javadoc::memberComment)
                                       .filter(text -> !text.isBlank())
                                       .findFirst()
                                       .orElse("");
        return Deprecation.addTag(comment, property.elements());
    }
}
