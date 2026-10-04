package io.github.manoelcampos.java2ts.parser.type;

import java.lang.reflect.Type;
import java.util.ArrayDeque;

/**
 * Creates normalized names of Java types, so that names written by users
 * (such as {@code java.util.List<java.util.List<java.lang.String>>} or {@code java.util.List[java.lang.String]})
 * can be compared with the names of types got via reflection.
 * @author Manoel Campos
 */
public final class TypeNames {
    private TypeNames() {/**/}

    /**
     * {@return the normalized name of a type, including its generic type arguments}
     * @param type the type to get the name
     */
    public static String of(final Type type) {
        return normalize(type.getTypeName());
    }

    /**
     * Normalizes a type name by removing white spaces, using {@code .} as separator for nested classes
     * and converting generic type arguments written between square brackets
     * (such as {@code Map[String, List[Integer]]}) to angle brackets (such as {@code Map<String,List<Integer>>}).
     * Square brackets used to represent arrays (such as {@code String[]}) are kept.
     * @param typeName the type name to normalize
     * @return the normalized type name
     */
    public static String normalize(final String typeName) {
        final String name = typeName.replaceAll("\\s+", "").replace('$', '.');
        final var result = new StringBuilder(name.length());
        final var openBrackets = new ArrayDeque<Character>();
        for (int i = 0; i < name.length(); i++) {
            result.append(convertBracket(name, i, openBrackets));
        }

        return result.toString();
    }

    /**
     * Converts a square bracket at a given position into an angle bracket,
     * when the bracket encloses generic type arguments.
     * @param name the type name
     * @param index the index of the char to convert
     * @param openBrackets a stack of the brackets opened so far (already converted)
     * @return the converted char
     */
    private static char convertBracket(final String name, final int index, final ArrayDeque<Character> openBrackets) {
        final char ch = name.charAt(index);
        final boolean isGenericOpening = ch == '[' && index + 1 < name.length() && name.charAt(index + 1) != ']';
        return switch (ch) {
            case '[' -> {
                final char converted = isGenericOpening ? '<' : '[';
                openBrackets.push(converted);
                yield converted;
            }
            case ']' -> openBrackets.isEmpty() || openBrackets.pop() == '[' ? ']' : '>';
            default -> ch;
        };
    }
}
