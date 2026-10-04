package io.github.manoelcampos.java2ts.ts;

import io.github.manoelcampos.java2ts.config.NullabilityDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TsTypeTest {
    private static final TsType STRING = TsBasicType.STRING;

    @Test
    void formatsLiterals() {
        assertEquals("\"A\\\"B\\\\C\"", TsBasicType.literal("A\"B\\C").format());
    }

    @Test
    void formatsArraysOfUnionsWithParentheses() {
        assertEquals("string[]", new TsArrayType(STRING).format());
        assertEquals("(string | null)[]", new TsArrayType(TsNullableType.of(STRING, NullabilityDefinition.nullInlineUnion)).format());
    }

    @Test
    void combinesUnionsRemovingDuplicates() {
        final TsType union = TsUnionType.combine(TsUnionType.combine(STRING, List.of("null")), List.of("null", "undefined"));
        assertEquals("string | null | undefined", union.format());
        assertSame(STRING, TsUnionType.combine(STRING, List.of()));
        assertEquals("string | undefined", ((TsUnionType) union).without("null").format());
    }

    @Test
    void doesNotNestNullableTypes() {
        final TsType nullable = TsNullableType.of(STRING, NullabilityDefinition.nullUnion);
        assertSame(nullable, TsNullableType.of(nullable, NullabilityDefinition.nullUnion));
        assertEquals("Nullable<string>", nullable.format());
        assertEquals(List.of(nullable), nullable.unionMembers());
    }

    @Test
    void declaresNullableAlias() {
        assertEquals("export type Nullable<T> = T | undefined;\n", TsNullableType.aliasDeclaration(NullabilityDefinition.undefinedUnion).format());
    }

    @Test
    void formatsReferencesAndMaps() {
        assertEquals("Page<string, T>", new TsReferenceType("Page", List.of(STRING, new TsReferenceType("T"))).format());
        assertEquals("Record<string, number>", new TsMapType(STRING, TsBasicType.NUMBER, false).format());
        assertEquals("Partial<Record<Color, number>>", new TsMapType(new TsReferenceType("Color"), TsBasicType.NUMBER, true).format());
    }

    @Test
    void formatsPropertiesWithInvalidIdentifiersAndComments() {
        assertEquals("    \"first-name\"?: string;", new TsProperty("first-name", STRING, true, false, "").format());
        assertEquals("    /**\n     * The name.\n     */\n    $name: string;", new TsProperty("$name", STRING, false, false, "The name.").format());
    }

    @Test
    void formatsReadonlyProperties() {
        assertEquals("    readonly name?: string;", new TsProperty("name", STRING, true, true, "").format());
    }

    @Test
    void formatsInterfaces() {
        final var props = List.of(new TsProperty("id", TsBasicType.NUMBER, false, true, ""));
        final var tsInterface = new TsInterface("Dog", List.of("T"), List.of(new TsReferenceType("Animal"), new TsReferenceType("Pet")), props, "A dog.");
        assertEquals("/**\n * A dog.\n */\nexport interface Dog<T> extends Animal, Pet {\n    readonly id: number;\n}\n", tsInterface.format());
    }

    @Test
    void formatsModelSortingDeclarations() {
        final var model = new TsModel(List.of(
            new TsTypeAlias("A", List.of(), STRING, ""),
            new TsInterface("Z", List.of(), List.of(), List.of(), ""),
            new TsInterface("B", List.of(), List.of(), List.of(), "")));

        assertEquals("export interface B {\n}\n\nexport interface Z {\n}\n\nexport type A = string;\n", model.format());
    }
}
