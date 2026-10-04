package io.github.manoelcampos.java2ts.scan;

import io.github.manoelcampos.java2ts.config.ClassSelection;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Checks if a class is excluded from conversion, according to the
 * {@link ClassSelection#excludeClasses()} and {@link ClassSelection#excludeClassPatterns()}.
 *
 * @param classNames fully qualified names of excluded classes
 * @param patterns patterns of excluded classes
 * @author Manoel Campos
 */
public record ExclusionFilter(Set<String> classNames, List<ClassPattern> patterns) implements Predicate<Class<?>> {
    /**
     * Creates a {@link ExclusionFilter}, validating the components and making immutable copies of collections.
     */
    public ExclusionFilter {
        classNames = Set.copyOf(classNames);
        patterns = List.copyOf(patterns);
    }

    /**
     * Creates an exclusion filter from the class selection settings.
     * @param selection the class selection settings
     */
    public ExclusionFilter(final ClassSelection selection) {
        this(Set.copyOf(selection.excludeClasses()), selection.excludeClassPatterns().stream().map(ClassPattern::new).toList());
    }

    /**
     * {@return true if the class is excluded, false otherwise}
     * @param aClass the class to check
     */
    @Override
    public boolean test(final Class<?> aClass) {
        final String name = aClass.getName();
        return classNames.contains(name) || patterns.stream().anyMatch(pattern -> pattern.matches(name));
    }
}
