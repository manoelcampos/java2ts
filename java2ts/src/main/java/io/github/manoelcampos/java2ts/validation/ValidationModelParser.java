package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.config.ValidationSettings;
import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.parser.EnumDeclarationParser;
import io.github.manoelcampos.java2ts.parser.JavaProperty;
import io.github.manoelcampos.java2ts.parser.ParsedModel;
import io.github.manoelcampos.java2ts.parser.PropertyExtractor;
import io.github.manoelcampos.java2ts.parser.PropertyResolver;
import io.github.manoelcampos.java2ts.parser.type.TsNames;
import io.github.manoelcampos.java2ts.parser.type.TypeContext;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.parser.type.TypeMapperFactory;
import io.github.manoelcampos.java2ts.scan.ClassPattern;
import io.github.manoelcampos.java2ts.scan.ExclusionFilter;
import io.github.manoelcampos.java2ts.ts.TsDeclaration;
import io.github.manoelcampos.java2ts.ts.TsInterface;
import io.github.manoelcampos.java2ts.validation.model.EnumSchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.ObjectSchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.ObjectSchemaDeclaration.TypeParameter;
import io.github.manoelcampos.java2ts.validation.model.SchemaDeclaration;
import io.github.manoelcampos.java2ts.validation.model.SchemaProperty;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;
import io.github.manoelcampos.java2ts.validation.model.ValidationModel;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Converts the Java classes declared in TypeScript into a {@link ValidationModel},
 * with one schema for each class, except the ones excluded from validation
 * (whose schemas are written by hand).
 * @author Manoel Campos
 */
final class ValidationModelParser {
    private final Settings settings;
    private final ValidationSettings validation;
    private final Predicate<Class<?>> tsExclusion;
    private final SchemaTypeRenderer renderer;
    private final SchemaPropertyResolver propertyResolver;

    /**
     * Creates a validation model parser.
     * @param settings the conversion settings
     * @param tsExclusion checks if a class is excluded from the TypeScript conversion
     */
    ValidationModelParser(final Settings settings, final Predicate<Class<?>> tsExclusion) {
        this.settings = settings;
        this.validation = settings.validation();
        this.tsExclusion = tsExclusion;
        this.renderer = new SchemaTypeRenderer(settings.nullabilityDefinition(), settings.mapDate(), new ConstraintReader());
        final var tsContext = new TypeContext(aClass -> {}, tsExclusion, settings.nullableAnnotations());
        final var tsResolver = new PropertyResolver(settings, TypeMapperFactory.create(settings, tsContext), Javadoc.NONE);
        this.propertyResolver = new SchemaPropertyResolver(tsResolver, renderer);
    }

    /**
     * Creates the schemas for the classes of a model.
     * @param model the classes converted to TypeScript
     * @param declarationsModule the module specifier from where the TypeScript types are imported
     * @return the validation model
     * @throws UnsupportedValidationException if any type or constraint can't be converted
     */
    ValidationModel parse(final ParsedModel model, final String declarationsModule) {
        final var exclusion = new ExclusionFilter(
            Set.copyOf(validation.excludeClasses()), validation.excludeClassPatterns().stream().map(ClassPattern::new).toList());
        final Map<String, TsDeclaration> tsDeclarations = model.tsModel().declarations().stream()
            .collect(Collectors.toMap(TsDeclaration::name, Function.identity()));

        final List<SchemaDeclaration> declarations = model.declaredClasses().stream()
            .filter(aClass -> !exclusion.test(aClass))
            .map(aClass -> declaration(aClass, tsDeclarations))
            .toList();

        final Set<String> excludedNames = model.declaredClasses().stream().filter(exclusion).map(TsNames::of).collect(Collectors.toSet());
        final Set<String> customSchemaTypes = referencedTypes(declarations).filter(excludedNames::contains).collect(Collectors.toSet());
        checkCustomSchemasModule(customSchemaTypes);
        return new ValidationModel(
            declarations, declarationsModule, customSchemaTypes, validation.customSchemasModule(),
            validation.schemaNameSuffix(), validation.zodConfig() ? validation.locale() : Optional.empty());
    }

    private SchemaDeclaration declaration(final Class<?> aClass, final Map<String, TsDeclaration> tsDeclarations) {
        final String typeName = TsNames.of(aClass);
        if (aClass.isEnum())
            return new EnumSchemaDeclaration(typeName, EnumDeclarationParser.constantNames(aClass));

        checkClassConstraints(aClass);
        final TypeMapper<SchemaType> mapper = mapper(aClass);
        final List<SchemaProperty> properties = PropertyExtractor.propertiesOf(aClass).stream()
                                                                 .map(property -> resolve(aClass, property, mapper))
                                                                 .toList();
        final List<String> tsTypeParameters = tsDeclarations.get(typeName) instanceof TsInterface tsInterface ? tsInterface.typeParameters() : List.of();
        return new ObjectSchemaDeclaration(typeName, typeParameters(aClass, tsTypeParameters), properties);
    }

    private TypeMapper<SchemaType> mapper(final Class<?> aClass) {
        final var context = new TypeContext(type -> {}, tsExclusion, settings.nullableAnnotations());
        final var customMappings = new CustomSchemaMappings(settings.customTypeMappings(), validation.customTypeMappings());
        return TypeMapperFactory.create(context, renderer, List.of(new TypeVariableBindings(aClass), customMappings));
    }

    private SchemaProperty resolve(final Class<?> aClass, final JavaProperty property, final TypeMapper<SchemaType> mapper) {
        try {
            return propertyResolver.resolve(property, mapper);
        } catch (final UnsupportedValidationException e) {
            throw e.at("%s.%s".formatted(aClass.getName(), property.name()));
        }
    }

    /**
     * Fails if a class has constraints, since they usually compare multiple properties and can't be converted.
     */
    private static void checkClassConstraints(final Class<?> aClass) {
        Arrays.stream(aClass.getAnnotations())
              .filter(ConstraintReader::isConstraint)
              .findFirst()
              .ifPresent(annotation -> {
                  throw ConstraintReader.unsupported(annotation.annotationType().getSimpleName()).at(aClass.getName());
              });
    }

    /**
     * {@return the type parameters of a class, with the bounds declared in its TypeScript interface}
     */
    private static List<TypeParameter> typeParameters(final Class<?> aClass, final List<String> declarations) {
        final TypeVariable<?>[] variables = aClass.getTypeParameters();
        return IntStream.range(0, variables.length)
                        .mapToObj(i -> new TypeParameter(variables[i].getName(), i < declarations.size() ? declarations.get(i) : variables[i].getName()))
                        .toList();
    }

    private void checkCustomSchemasModule(final Set<String> customSchemaTypes) {
        if (!customSchemaTypes.isEmpty() && validation.customSchemasModule().isEmpty()) {
            final var msg = "Schemas of %s are excluded from validation but referenced by other schemas. Set the customSchemasModule to import them";
            throw new UnsupportedValidationException(msg.formatted(String.join(", ", customSchemaTypes)));
        }
    }

    private static Stream<String> referencedTypes(final List<SchemaDeclaration> declarations) {
        return declarations.stream()
                           .flatMap(declaration -> declaration instanceof ObjectSchemaDeclaration object ? object.properties().stream() : Stream.empty())
                           .flatMap(property -> property.type().referencedTypes());
    }
}
