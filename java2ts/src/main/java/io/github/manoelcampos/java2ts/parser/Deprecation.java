package io.github.manoelcampos.java2ts.parser;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

/**
 * Adds the JSDoc {@code @deprecated} tag to the documentation of elements annotated with {@link Deprecated},
 * so that TypeScript editors show them as deprecated.
 * @author Manoel Campos
 */
final class Deprecation {
    private static final String TAG = "@deprecated";

    private Deprecation() {/**/}

    /**
     * Adds the {@code @deprecated} tag to a comment if any of the elements is deprecated.
     * @param comment the documentation comment
     * @param elements the elements to check
     * @return the comment including the {@code @deprecated} tag if any element is deprecated
     *         and the comment doesn't have that tag yet; the original comment otherwise
     */
    static String addTag(final String comment, final Collection<? extends AnnotatedElement> elements) {
        final boolean deprecated = elements.stream().anyMatch(element -> element.isAnnotationPresent(Deprecated.class));
        if (!deprecated || comment.contains(TAG))
            return comment;

        return comment.isBlank() ? TAG : comment + "\n" + TAG;
    }
}
