package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.parser.type.TypeContext;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.parser.type.TypeMapperFactory;
import io.github.manoelcampos.java2ts.ts.TsDeclaration;
import io.github.manoelcampos.java2ts.ts.TsModel;
import io.github.manoelcampos.java2ts.ts.TsNullableType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Converts Java classes into a {@link TsModel} with their TypeScript declarations.
 * Classes referenced by the converted ones (such as supertypes and property types) are converted too.
 * Each instance must be used for a single conversion.
 * @author Manoel Campos
 */
public final class ModelParser {
    private final Settings settings;
    private final Queue<Class<?>> pending = new ArrayDeque<>();
    private final Set<Class<?>> visited = new HashSet<>();
    private final Map<String, Class<?>> classesByTsName = new HashMap<>();
    private final DeclarationParser interfaceParser;
    private final DeclarationParser enumParser;

    /**
     * Creates a model parser.
     * @param settings the conversion settings
     * @param javadoc where to get documentation from
     * @param exclusion checks if a class is excluded from conversion
     */
    public ModelParser(final Settings settings, final Javadoc javadoc, final Predicate<Class<?>> exclusion) {
        this.settings = settings;
        final var context = new TypeContext(pending::add, exclusion, settings.nullableAnnotations(), settings.nullabilityDefinition());
        final TypeMapper typeMapper = TypeMapperFactory.create(settings, context);
        this.interfaceParser = new InterfaceDeclarationParser(typeMapper, new PropertyResolver(settings, typeMapper, javadoc), javadoc);
        this.enumParser = new EnumDeclarationParser(javadoc);
    }

    /**
     * Converts classes to TypeScript declarations.
     * @param classes the classes to convert
     * @return the model with the declarations for the given classes and the ones they reference
     * @throws IllegalStateException if two different classes have the same TypeScript name
     */
    public TsModel parse(final Collection<Class<?>> classes) {
        pending.addAll(classes);
        final var declarations = new ArrayList<TsDeclaration>();
        while (!pending.isEmpty()) {
            final Class<?> next = pending.remove();
            if (visited.add(next))
                declarations.add(parse(next));
        }

        addNullableAlias(declarations);
        return new TsModel(declarations);
    }

    private TsDeclaration parse(final Class<?> aClass) {
        final DeclarationParser parser = aClass.isEnum() ? enumParser : interfaceParser;
        final TsDeclaration declaration = parser.parse(aClass);
        checkUniqueName(declaration.name(), aClass);
        return declaration;
    }

    private void checkUniqueName(final String tsName, final Class<?> aClass) {
        final Class<?> previous = classesByTsName.putIfAbsent(tsName, aClass);
        if (previous != null) {
            final var msg = "Classes %s and %s would generate TypeScript declarations with the same name %s. Exclude one of them.";
            throw new IllegalStateException(msg.formatted(previous.getName(), aClass.getName(), tsName));
        }
    }

    /**
     * Adds the declaration of the {@code Nullable<T>} type alias when it's used.
     */
    private void addNullableAlias(final List<TsDeclaration> declarations) {
        if (settings.nullabilityDefinition().isInline())
            return;

        final boolean used = declarations.stream().anyMatch(declaration -> declaration.format().contains(TsNullableType.ALIAS_NAME + "<"));
        if (used)
            declarations.add(TsNullableType.aliasDeclaration(settings.nullabilityDefinition()));
    }
}
