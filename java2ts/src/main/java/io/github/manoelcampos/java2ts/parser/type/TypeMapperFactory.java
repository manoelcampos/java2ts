package io.github.manoelcampos.java2ts.parser.type;

import io.github.manoelcampos.java2ts.config.Settings;

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
     * Creates a type mapper.
     * @param settings the conversion settings
     * @param context the information shared by the rules
     * @return the new type mapper
     */
    public static TypeMapper create(final Settings settings, final TypeContext context) {
        final List<TypeMappingRule> rules = List.of(
            new CustomTypeMappings(settings.customTypeMappings()),
            new TypeVariableRule(),
            new WildcardRule(),
            new BasicTypeRule(),
            new ArrayRule(),
            new DateRule(settings.mapDate()),
            new OptionalRule(),
            new CollectionRule(),
            new MapRule(),
            new UndeclarableTypeRule(),
            new DeclaredTypeRule()
        );

        return new TypeMapper(context, rules);
    }
}
