package io.github.manoelcampos.java2ts.parser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extracts the properties of a record, which are its components.
 * @author Manoel Campos
 */
public final class RecordPropertyExtractor implements PropertyExtractor {
    /**
     * Creates a {@link RecordPropertyExtractor}.
     */
    public RecordPropertyExtractor() {/**/}

    @Override
    public List<JavaProperty> extract(final Class<?> aClass) {
        return Arrays.stream(aClass.getRecordComponents()).map(RecordPropertyExtractor::toProperty).toList();
    }

    private static JavaProperty toProperty(final RecordComponent component) {
        final var elements = new ArrayList<AnnotatedElement>();
        Members.declaredField(component.getDeclaringRecord(), component.getName()).ifPresent(elements::add);
        elements.add(component);
        elements.add(component.getAccessor());
        return new JavaProperty(component.getName(), component.getAnnotatedType(), elements);
    }
}
