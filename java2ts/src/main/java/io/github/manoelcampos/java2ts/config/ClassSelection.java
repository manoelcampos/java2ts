package io.github.manoelcampos.java2ts.config;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Settings that define which Java classes are converted to TypeScript.
 * Classes referenced by the selected ones (such as supertypes and property types) are converted too,
 * unless they are excluded.
 *
 * @param classes fully qualified names of classes to convert
 * @param classPatterns glob patterns of classes to convert, where {@code *} matches any chars
 *                      inside a single package/class name and {@code **} matches any chars
 *                      (such as {@code com.company.model.**})
 * @param excludeClasses fully qualified names of classes that must not be converted
 * @param excludeClassPatterns glob patterns of classes that must not be converted
 * @param classesWithAnnotations fully qualified names of annotations; the project classes annotated
 *                               with any of them are converted
 * @param classesImplementingInterfaces fully qualified names of interfaces; the project classes
 *                                      implementing any of them are converted
 * @param classesExtendingClasses fully qualified names of classes; the project classes extending
 *                                any of them are converted
 * @author Manoel Campos
 */
public record ClassSelection(
    List<String> classes,
    List<String> classPatterns,
    List<String> excludeClasses,
    List<String> excludeClassPatterns,
    List<String> classesWithAnnotations,
    List<String> classesImplementingInterfaces,
    List<String> classesExtendingClasses)
{
    /** A selection that selects no class. */
    public static final ClassSelection EMPTY = of(null, null, null, null, null, null, null);

    /**
     * Creates a {@link ClassSelection}, validating the components and making immutable copies of collections.
     */
    public ClassSelection {
        classes = List.copyOf(classes);
        classPatterns = List.copyOf(classPatterns);
        excludeClasses = List.copyOf(excludeClasses);
        excludeClassPatterns = List.copyOf(excludeClassPatterns);
        classesWithAnnotations = List.copyOf(classesWithAnnotations);
        classesImplementingInterfaces = List.copyOf(classesImplementingInterfaces);
        classesExtendingClasses = List.copyOf(classesExtendingClasses);
    }

    /**
     * Creates a class selection where null lists are replaced by empty ones.
     * The parameters have the same meaning of the record components.
     * @return a new class selection
     */
    public static ClassSelection of(
        final @Nullable List<String> classes,
        final @Nullable List<String> classPatterns,
        final @Nullable List<String> excludeClasses,
        final @Nullable List<String> excludeClassPatterns,
        final @Nullable List<String> classesWithAnnotations,
        final @Nullable List<String> classesImplementingInterfaces,
        final @Nullable List<String> classesExtendingClasses)
    {
        return new ClassSelection(
            orEmpty(classes), orEmpty(classPatterns), orEmpty(excludeClasses), orEmpty(excludeClassPatterns),
            orEmpty(classesWithAnnotations), orEmpty(classesImplementingInterfaces), orEmpty(classesExtendingClasses));
    }

    /**
     * {@return a class selection with just class patterns}
     * @param classPatterns the patterns of the classes to select
     */
    public static ClassSelection ofPatterns(final List<String> classPatterns) {
        return of(null, classPatterns, null, null, null, null, null);
    }

    /**
     * {@return true if any of the selection criteria require scanning the project classes,
     * instead of just matching class names}
     */
    public boolean requiresTypeScan() {
        return !classesWithAnnotations.isEmpty() || !classesImplementingInterfaces.isEmpty() || !classesExtendingClasses.isEmpty();
    }

    private static List<String> orEmpty(final @Nullable List<String> list) {
        return list == null ? List.of() : list;
    }
}
