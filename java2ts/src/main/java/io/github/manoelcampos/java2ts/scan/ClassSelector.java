package io.github.manoelcampos.java2ts.scan;

import io.github.manoelcampos.java2ts.config.ClassSelection;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * Selects the classes to be converted to TypeScript according to the {@link ClassSelection} settings.
 * Classes defined by name or pattern are looked up in all the classpath entries,
 * while classes selected by annotations or supertypes are looked up only in the
 * classpath directories (the project's own compiled classes), avoiding loading all classes from jar files.
 *
 * @author Manoel Campos
 */
public final class ClassSelector {
    private final ClassSelection selection;
    private final ClassPathContext context;
    private final ClassLoading loading;

    /**
     * Creates a class selector.
     * @param selection the settings defining which classes to select
     * @param context the classpath where classes are looked up
     */
    public ClassSelector(final ClassSelection selection, final ClassPathContext context) {
        this.selection = selection;
        this.context = context;
        this.loading = new ClassLoading(context.classLoader());
    }

    /**
     * {@return the selected classes, sorted by name}
     * @throws IllegalArgumentException if any class explicitly selected by name is not found
     */
    public SequencedSet<Class<?>> select() {
        final var exclusion = new ExclusionFilter(selection);
        return Stream.of(explicitClasses(), patternClasses(), typeScanClasses())
                     .flatMap(stream -> stream)
                     .filter(ClassSelector::isConvertible)
                     .filter(not(exclusion))
                     .sorted(Comparator.comparing(Class::getName))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@return true if a class can be converted to TypeScript, false otherwise}
     * Annotations, anonymous, local and synthetic classes are not convertible.
     * @param aClass the class to check
     */
    public static boolean isConvertible(final Class<?> aClass) {
        return !aClass.isAnnotation() && !aClass.isAnonymousClass() && !aClass.isLocalClass()
               && !aClass.isSynthetic() && !aClass.isPrimitive() && !aClass.isArray();
    }

    private Stream<Class<?>> explicitClasses() {
        return selection.classes().stream().map(loading::load);
    }

    private Stream<Class<?>> patternClasses() {
        if (selection.classPatterns().isEmpty())
            return Stream.empty();

        final List<ClassPattern> patterns = selection.classPatterns().stream().map(ClassPattern::new).toList();
        return context.entries().stream()
                      .flatMap(entry -> ClasspathScanner.classNames(entry).stream())
                      .filter(name -> patterns.stream().anyMatch(pattern -> pattern.matches(name)))
                      .map(loading::tryLoad)
                      .flatMap(Optional::stream);
    }

    private Stream<Class<?>> typeScanClasses() {
        if (!selection.requiresTypeScan())
            return Stream.empty();

        return context.entries().stream()
                      .filter(Files::isDirectory)
                      .flatMap(entry -> ClasspathScanner.classNames(entry).stream())
                      .map(loading::tryLoad)
                      .flatMap(Optional::stream)
                      .filter(typeCriteria());
    }

    private Predicate<Class<?>> typeCriteria() {
        final Predicate<Class<?>> annotated = this::hasAnyAnnotation;
        return annotated.or(aClass -> hasAnySupertype(aClass, selection.classesImplementingInterfaces()))
                        .or(aClass -> hasAnySupertype(aClass, selection.classesExtendingClasses()));
    }

    private boolean hasAnyAnnotation(final Class<?> aClass) {
        return Arrays.stream(aClass.getAnnotations())
                     .anyMatch(annotation -> selection.classesWithAnnotations().contains(annotation.annotationType().getName()));
    }

    private static boolean hasAnySupertype(final Class<?> aClass, final List<String> superTypeNames) {
        return superTypeNames.stream().anyMatch(name -> TypeHierarchy.hasSupertype(aClass, name));
    }
}
