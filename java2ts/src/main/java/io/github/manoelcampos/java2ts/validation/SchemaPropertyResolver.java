package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.parser.JavaProperty;
import io.github.manoelcampos.java2ts.parser.PropertyResolver;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.ts.TsProperty;
import io.github.manoelcampos.java2ts.ts.TsType;
import io.github.manoelcampos.java2ts.validation.model.NullableSchema;
import io.github.manoelcampos.java2ts.validation.model.SchemaProperty;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Converts Java properties into schema properties.
 * Whether a property is optional and nullable is taken from the TypeScript property
 * generated for it, so that the schema always matches the TypeScript declaration.
 * @author Manoel Campos
 */
final class SchemaPropertyResolver {
    private static final String NULL = "null";
    private static final String UNDEFINED = "undefined";
    private final PropertyResolver tsResolver;
    private final SchemaTypeRenderer renderer;

    /**
     * Creates a schema property resolver.
     * @param tsResolver converts Java properties to TypeScript ones
     * @param renderer creates schemas, including the constraints from annotations
     */
    SchemaPropertyResolver(final PropertyResolver tsResolver, final SchemaTypeRenderer renderer) {
        this.tsResolver = tsResolver;
        this.renderer = renderer;
    }

    /**
     * {@return the schema property for a Java property}
     * @param property the Java property to convert
     * @param mapper converts the property type to a schema
     */
    SchemaProperty resolve(final JavaProperty property, final TypeMapper<SchemaType> mapper) {
        final TsProperty tsProperty = tsResolver.resolve(property);
        final SchemaType type = NullableSchema.unwrap(mapper.map(property.type()));
        final SchemaType constrained = renderer.constrain(type, declarationAnnotations(property));
        final TsType tsType = tsProperty.type();
        return new SchemaProperty(property.name(), constrained, tsProperty.optional(), tsType.includes(NULL), tsType.includes(UNDEFINED));
    }

    /**
     * {@return the annotations of the elements defining a property (such as its field and getters)},
     * except the ones on the property type, which were already applied when the type was converted.
     * An annotation on a field may be both a declaration and a type annotation, such as {@code @NotBlank String name}.
     */
    private static List<Annotation> declarationAnnotations(final JavaProperty property) {
        final Set<Annotation> typeAnnotations = Set.of(property.type().getAnnotations());
        return property.elements().stream()
                       .map(AnnotatedElement::getAnnotations)
                       .flatMap(Arrays::stream)
                       .filter(annotation -> !typeAnnotations.contains(annotation))
                       .distinct()
                       .toList();
    }
}
