package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.ts.TsType;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link TypeMapper}s with the rules in the order they must be tried.
 * The order matters: more specific rules come first and the {@link DeclaredTypeRule}
 * must be the last one, since it accepts any class.
 * @author Manoel Campos
 */
public final class TypeMapperFactory {
    private TypeMapperFactory() {/**/}

    /**
     * Creates a type mapper that converts Java types to TypeScript.
     * @param settings the conversion settings
     * @param context the information shared by the rules
     * @return the new type mapper
     */
    public static TypeMapper<TsType> create(final Settings settings, final TypeContext context) {
        final var renderer = new TsTypeRenderer(settings.nullabilityDefinition(), settings.mapDate());
        return create(context, renderer, List.of(new CustomTypeMappings<>(settings.customTypeMappings())));
    }

    /**
     * Creates a type mapper for any kind of result.
     * @param context the information shared by the rules
     * @param renderer creates the results for the types classified by the rules
     * @param firstRules rules tried before the standard ones (such as custom type mappings)
     * @param <R> the type of the result
     * @return the new type mapper
     */
    public static <R> TypeMapper<R> create(
        final TypeContext context, final TypeRenderer<R> renderer, final List<TypeMappingRule<R>> firstRules)
    {
        final var rules = new ArrayList<>(firstRules);
        rules.addAll(List.of(
            new TypeVariableRule<>(),
            new WildcardRule<>(),
            new BasicTypeRule<>(),
            new ArrayRule<>(),
            new DateRule<>(),
            new OptionalRule<>(),
            new CollectionRule<>(),
            new MapRule<>(),
            new UndeclarableTypeRule<>(),
            new DeclaredTypeRule<>()
        ));

        return new TypeMapper<>(context, renderer, rules);
    }
}
