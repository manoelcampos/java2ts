package io.github.manoelcampos.java2ts.parser;

import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.parser.type.TsNames;
import io.github.manoelcampos.java2ts.ts.TsBasicType;
import io.github.manoelcampos.java2ts.ts.TsDeclaration;
import io.github.manoelcampos.java2ts.ts.TsType;
import io.github.manoelcampos.java2ts.ts.TsTypeAlias;
import io.github.manoelcampos.java2ts.ts.TsUnionType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Converts Java enums to TypeScript string literal union types,
 * such as {@code export type Color = "RED" | "BLUE";}, since enums are serialized by their names.
 * Enum constants annotated with {@code @JsonProperty("name")} use the given name.
 * @author Manoel Campos
 */
public final class EnumDeclarationParser implements DeclarationParser {
    private static final TsType NEVER = new TsBasicType("never");
    private final Javadoc javadoc;

    /**
     * Creates an enum parser.
     * @param javadoc where to get the enum documentation from
     */
    public EnumDeclarationParser(final Javadoc javadoc) {
        this.javadoc = javadoc;
    }

    @Override
    public TsDeclaration parse(final Class<?> aClass) {
        final List<TsType> literals = Arrays.stream(aClass.getDeclaredFields())
                                            .filter(Field::isEnumConstant)
                                            .map(EnumDeclarationParser::constantName)
                                            .<TsType>map(TsBasicType::literal)
                                            .toList();

        final TsType type = literals.isEmpty() ? NEVER : TsUnionType.of(literals);
        return new TsTypeAlias(TsNames.of(aClass), List.of(), type, Deprecation.addTag(javadoc.classComment(aClass), List.of(aClass)));
    }

    private static String constantName(final Field constant) {
        return JacksonAnnotations.renamedName(List.of(constant)).orElse(constant.getName());
    }
}
