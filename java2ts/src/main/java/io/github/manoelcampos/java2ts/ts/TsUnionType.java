package io.github.manoelcampos.java2ts.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A TypeScript union type, such as {@code string | null}.
 * @param types the types in the union (always more than one)
 * @author Manoel Campos
 */
public record TsUnionType(List<TsType> types) implements TsType {
    /**
     * Creates a {@link TsUnionType}, validating the components and making immutable copies of collections.
     */
    public TsUnionType {
        types = List.copyOf(types);
    }

    /**
     * Creates a type that is the union of a base type with other ones,
     * flattening nested unions and removing duplicates.
     * @param base the base type
     * @param others names of the other types to add (such as {@code null} and {@code undefined})
     * @return the union type, or the base type itself when there is nothing to add
     */
    public static TsType combine(final TsType base, final Collection<String> others) {
        final var types = new ArrayList<>(base.unionMembers());
        others.stream().map(TsBasicType::new).forEach(types::add);
        return of(types);
    }

    /**
     * Creates a type from a list of types, flattening nested unions and removing duplicates.
     * @param types the types to join
     * @return a union with the given types, the only type in the list if there is just one,
     *         or {@link TsType#ANY} if any of the types is {@code any} (since it already includes all others)
     */
    public static TsType of(final List<TsType> types) {
        if (types.contains(TsType.ANY))
            return TsType.ANY;

        final var unique = new LinkedHashMap<String, TsType>();
        types.stream()
             .flatMap(type -> type.unionMembers().stream())
             .forEach(type -> unique.putIfAbsent(type.format(), type));

        final var list = List.copyOf(unique.values());
        return list.size() == 1 ? list.getFirst() : new TsUnionType(list);
    }

    /**
     * {@return a type that is this union without the given type, or the remaining single type}
     * @param typeName the name of the type to remove (such as {@code undefined})
     */
    public TsType without(final String typeName) {
        return of(types.stream().filter(type -> !type.format().equals(typeName)).toList());
    }

    @Override
    public List<TsType> unionMembers() {
        return types;
    }

    @Override
    public String format() {
        return types.stream().map(TsType::format).collect(Collectors.joining(" | "));
    }
}
