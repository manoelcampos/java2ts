package io.github.manoelcampos.java2ts.ts;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A TypeScript interface declaration, generated from a Java class, record or interface.
 * @param name the interface name
 * @param typeParameters the names of the generic type parameters
 * @param superTypes the interfaces that this one extends
 * @param properties the properties declared by this interface (excluding the inherited ones)
 * @param comment the interface documentation (may be empty)
 * @author Manoel Campos
 */
public record TsInterface(
    String name, List<String> typeParameters, List<TsType> superTypes,
    List<TsProperty> properties, String comment) implements TsDeclaration
{
    /**
     * Creates a {@link TsInterface}, validating the components and making immutable copies of collections.
     */
    public TsInterface {
        requireNonNull(name);
        typeParameters = List.copyOf(typeParameters);
        superTypes = List.copyOf(superTypes);
        properties = List.copyOf(properties);
        comment = requireNonNullElse(comment, "");
    }

    @Override
    public String format() {
        final String props = properties.stream().map(prop -> prop.format() + "\n").collect(Collectors.joining());
        return "%sexport interface %s%s%s {\n%s}\n".formatted(
            JsDoc.format(comment, ""), name, TsDeclaration.formatTypeParameters(typeParameters), formatExtends(), props);
    }

    private String formatExtends() {
        if (superTypes.isEmpty())
            return "";

        return " extends " + superTypes.stream().map(TsType::format).collect(Collectors.joining(", "));
    }

    @Override
    public int kindOrder() {
        return 0;
    }
}
