package io.github.manoelcampos.java2ts.validation.model;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The schema of a value, independent of any validation library.
 * @author Manoel Campos
 */
public sealed interface SchemaType
    permits ConstrainedSchema, RecordSchema, ReferenceSchema,
            TypeParameterSchema, CustomSchema, NullableSchema
{
    /**
     * {@return the kind of value this schema validates, used to check which constraints apply to it},
     * or an empty Optional if no constraint applies
     */
    default Optional<ConstraintTarget> constraintTarget() {
        return Optional.empty();
    }

    /**
     * {@return a description of this schema, used in error messages}
     */
    String describe();

    /**
     * {@return the names of the TypeScript types referenced by this schema (including inner schemas)},
     * whose schemas must be declared or imported
     */
    default Stream<String> referencedTypes() {
        return switch (this) {
            case ReferenceSchema reference -> Stream.concat(
                Stream.of(reference.typeName()), reference.typeArguments().stream().flatMap(SchemaType::referencedTypes));
            case ArraySchema array -> array.element().referencedTypes();
            case RecordSchema record -> Stream.concat(record.key().referencedTypes(), record.value().referencedTypes());
            case NullableSchema nullable -> nullable.type().referencedTypes();
            default -> Stream.empty();
        };
    }

    /**
     * Adds a constraint to this schema (or to the schema inside a {@link NullableSchema}).
     * @param constraint the constraint to add
     * @return a new schema including the constraint
     * @throws UnsupportedValidationException if the constraint doesn't apply to this schema
     */
    default SchemaType constrain(final Constraint constraint) {
        if (this instanceof NullableSchema nullable)
            return nullable.withType(nullable.type().constrain(constraint));

        final boolean applies = constraintTarget().filter(constraint.targets()::contains).isPresent();
        if (!applies || !(this instanceof ConstrainedSchema constrained))
            throw new UnsupportedValidationException("it doesn't apply to " + describe());

        return constrained.with(constraint);
    }
}
