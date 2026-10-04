package io.github.manoelcampos.java2ts.parser;

import java.util.List;

/**
 * Extracts the properties of a Java class (Strategy pattern).
 * @author Manoel Campos
 */
public sealed interface PropertyExtractor permits RecordPropertyExtractor, BeanPropertyExtractor {
    /**
     * Extracts all properties of a class, including the inherited ones,
     * without applying Jackson annotations.
     * @param aClass the class to extract properties from
     * @return the class properties
     */
    List<JavaProperty> extract(Class<?> aClass);

    /**
     * Extracts all properties of a class (including the inherited ones), according to its kind,
     * removing the properties ignored by {@code @JsonIgnore} and renaming the ones annotated with {@code @JsonProperty}.
     * @param aClass the class to extract properties from
     * @return the class properties
     */
    static List<JavaProperty> propertiesOf(final Class<?> aClass) {
        final PropertyExtractor extractor = aClass.isRecord() ? new RecordPropertyExtractor() : new BeanPropertyExtractor();
        return JacksonAnnotations.apply(extractor.extract(aClass));
    }
}
