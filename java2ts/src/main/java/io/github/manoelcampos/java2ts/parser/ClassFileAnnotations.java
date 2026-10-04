package io.github.manoelcampos.java2ts.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AttributedElement;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.TypeAnnotation;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads annotations with {@link java.lang.annotation.RetentionPolicy#CLASS CLASS} retention
 * (such as {@code lombok.NonNull}), which are stored in class files but are not available via reflection.
 * It uses the <a href="https://openjdk.org/jeps/484">ClassFile API</a> to parse the class files.
 * @author Manoel Campos
 */
final class ClassFileAnnotations {
    private static final Map<Class<?>, Map<String, Set<String>>> CACHE = new ConcurrentHashMap<>();
    private static final String METHOD_SUFFIX = "()";
    private static final String COMPONENT_PREFIX = "record:";

    private ClassFileAnnotations() {/**/}

    /**
     * Gets the names of the annotations with CLASS retention of a field, method without parameters or record component.
     * @param element the element to get the annotations
     * @return the fully qualified names of the annotations, or an empty set if the element has none
     *         or its class file cannot be read
     */
    static Set<String> of(final AnnotatedElement element) {
        return switch (element) {
            case Field field -> annotations(field, field.getName());
            case Method method when method.getParameterCount() == 0 -> annotations(method, method.getName() + METHOD_SUFFIX);
            case RecordComponent component -> annotations(component.getDeclaringRecord(), COMPONENT_PREFIX + component.getName());
            default -> Set.of();
        };
    }

    private static Set<String> annotations(final Member member, final String key) {
        return annotations(member.getDeclaringClass(), key);
    }

    private static Set<String> annotations(final Class<?> aClass, final String key) {
        return CACHE.computeIfAbsent(aClass, ClassFileAnnotations::readClass).getOrDefault(key, Set.of());
    }

    /**
     * {@return a map where each key identifies a class member and the value is the set of its CLASS retention annotations}
     */
    private static Map<String, Set<String>> readClass(final Class<?> aClass) {
        final String resource = aClass.getName().replace('.', '/') + ".class";
        final ClassLoader loader = aClass.getClassLoader();
        try (InputStream input = loader == null ? null : loader.getResourceAsStream(resource)) {
            return input == null ? Map.of() : membersAnnotations(ClassFile.of().parse(input.readAllBytes()));
        } catch (final IOException e) {
            throw new UncheckedIOException("Error reading class file of " + aClass.getName(), e);
        }
    }

    private static Map<String, Set<String>> membersAnnotations(final ClassModel model) {
        final var map = new HashMap<String, Set<String>>();
        model.fields().forEach(field -> map.put(field.fieldName().stringValue(), names(field)));
        model.methods().stream()
             .filter(method -> method.methodTypeSymbol().parameterCount() == 0)
             .forEach(method -> map.put(method.methodName().stringValue() + METHOD_SUFFIX, names(method)));
        model.findAttribute(Attributes.record()).ifPresent(record -> record.components().forEach(
            component -> map.put(COMPONENT_PREFIX + component.name().stringValue(), names(component))));
        return map;
    }

    /**
     * {@return the names of the invisible declaration and type annotations of a class file element}
     */
    private static Set<String> names(final AttributedElement element) {
        final Stream<Annotation> declaration = element.findAttribute(Attributes.runtimeInvisibleAnnotations()).stream()
                                                      .flatMap(attribute -> attribute.annotations().stream());
        final Stream<Annotation> typeUse = element.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).stream()
                                                  .flatMap(attribute -> attribute.annotations().stream())
                                                  .map(TypeAnnotation::annotation);

        return Stream.concat(declaration, typeUse)
                     .map(Annotation::classSymbol)
                     .map(ClassFileAnnotations::className)
                     .collect(Collectors.toUnmodifiableSet());
    }

    private static String className(final ClassDesc desc) {
        final String packageName = desc.packageName();
        return packageName.isEmpty() ? desc.displayName() : packageName + "." + desc.displayName();
    }
}
