package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.config.NullabilityDefinition;
import io.github.manoelcampos.java2ts.parser.type.BasicKind;
import io.github.manoelcampos.java2ts.parser.type.DateKind;
import io.github.manoelcampos.java2ts.parser.type.MapKeyKind;
import io.github.manoelcampos.java2ts.parser.type.TsNames;
import io.github.manoelcampos.java2ts.parser.type.TypeRenderer;
import io.github.manoelcampos.java2ts.validation.model.ArraySchema;
import io.github.manoelcampos.java2ts.validation.model.Constraint;
import io.github.manoelcampos.java2ts.validation.model.CustomSchema;
import io.github.manoelcampos.java2ts.validation.model.DateSchema;
import io.github.manoelcampos.java2ts.validation.model.NullableSchema;
import io.github.manoelcampos.java2ts.validation.model.RecordSchema;
import io.github.manoelcampos.java2ts.validation.model.ReferenceSchema;
import io.github.manoelcampos.java2ts.validation.model.ScalarSchema;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;
import io.github.manoelcampos.java2ts.validation.model.TypeParameterSchema;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.List;

/**
 * Creates validation schemas for Java types, including the constraints
 * defined by Bean Validation annotations on type usages (such as {@code List<@NotBlank String>}).
 * @author Manoel Campos
 */
public final class SchemaTypeRenderer implements TypeRenderer<SchemaType> {
    private static final String NULL = "null";
    private static final String UNDEFINED = "undefined";
    private final NullabilityDefinition nullability;
    private final DateMapping dateMapping;
    private final ConstraintReader constraintReader;

    /**
     * Creates a schema renderer.
     * @param nullability how nullable types are written in TypeScript, which defines if null and/or undefined are accepted
     * @param dateMapping how date/time types are written in TypeScript
     * @param constraintReader reads the constraints from Bean Validation annotations
     */
    public SchemaTypeRenderer(final NullabilityDefinition nullability, final DateMapping dateMapping, final ConstraintReader constraintReader) {
        this.nullability = nullability;
        this.dateMapping = dateMapping;
        this.constraintReader = constraintReader;
    }

    @Override
    public SchemaType any() {
        return new ScalarSchema(BasicKind.ANY);
    }

    @Override
    public SchemaType custom(final String expression) {
        return new CustomSchema(expression);
    }

    @Override
    public SchemaType typeVariable(final String name) {
        return new TypeParameterSchema(name);
    }

    @Override
    public SchemaType basic(final BasicKind kind) {
        return new ScalarSchema(kind);
    }

    @Override
    public SchemaType date(final DateKind kind) {
        return new DateSchema(kind, dateMapping);
    }

    @Override
    public SchemaType array(final SchemaType element) {
        return new ArraySchema(element);
    }

    @Override
    public SchemaType map(final MapKeyKind keyKind, final SchemaType key, final SchemaType value) {
        return new RecordSchema(keyKind, key, value);
    }

    @Override
    public SchemaType reference(final Class<?> aClass, final List<SchemaType> typeArguments) {
        return new ReferenceSchema(TsNames.of(aClass), typeArguments);
    }

    @Override
    public SchemaType nullable(final SchemaType type) {
        if (type instanceof NullableSchema)
            return type;

        return new NullableSchema(type, nullability.types().contains(NULL), nullability.types().contains(UNDEFINED));
    }

    @Override
    public SchemaType annotate(final SchemaType type, final AnnotatedType annotatedType) {
        return constrain(type, List.of(annotatedType.getAnnotations()));
    }

    /**
     * Adds to a schema the constraints defined by annotations.
     * @param type the schema to add constraints to
     * @param annotations the annotations to read the constraints from (the ones that aren't constraints are ignored)
     * @return the schema including the constraints
     * @throws UnsupportedValidationException if any constraint isn't supported or doesn't apply to the schema
     */
    public SchemaType constrain(final SchemaType type, final Collection<Annotation> annotations) {
        SchemaType result = type;
        for (final Annotation annotation : annotations)
            result = constrain(result, annotation);

        return result;
    }

    private SchemaType constrain(final SchemaType type, final Annotation annotation) {
        SchemaType result = type;
        try {
            for (final Constraint constraint : constraintReader.read(annotation))
                result = result.constrain(constraint);
        } catch (final UnsupportedValidationException e) {
            throw e.at("@" + annotation.annotationType().getSimpleName());
        }

        return result;
    }
}
