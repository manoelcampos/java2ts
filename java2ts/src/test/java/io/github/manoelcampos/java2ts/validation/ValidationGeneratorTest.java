package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.Java2Ts;
import io.github.manoelcampos.java2ts.TestClasspath;
import io.github.manoelcampos.java2ts.TestSettings;
import io.github.manoelcampos.java2ts.config.ClassSelection;
import io.github.manoelcampos.java2ts.config.DateMapping;
import io.github.manoelcampos.java2ts.config.OptionalPropertiesDeclaration;
import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.config.SettingsBuilder;
import io.github.manoelcampos.java2ts.config.ValidationSettings;
import io.github.manoelcampos.java2ts.config.ValidationSettingsBuilder;
import io.github.manoelcampos.java2ts.fixtures.Address;
import io.github.manoelcampos.java2ts.fixtures.EmptyEnum;
import io.github.manoelcampos.java2ts.validation.fixtures.Contact;
import io.github.manoelcampos.java2ts.validation.fixtures.Customer;
import io.github.manoelcampos.java2ts.validation.fixtures.CustomerPage;
import io.github.manoelcampos.java2ts.validation.fixtures.Dates;
import io.github.manoelcampos.java2ts.validation.fixtures.Odd;
import io.github.manoelcampos.java2ts.scan.ClassPathContext;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static io.github.manoelcampos.java2ts.TestSettings.FIXTURES_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests generating Zod schemas for the fixture classes.
 */
class ValidationGeneratorTest {
    private static final String ADDRESS = Address.class.getName();

    private static String generate(final List<Class<?>> classes, final UnaryOperator<ValidationSettingsBuilder> validation) {
        return generate(classes, validation, UnaryOperator.identity());
    }

    private static String generate(
        final List<Class<?>> classes, final UnaryOperator<ValidationSettingsBuilder> validation,
        final UnaryOperator<SettingsBuilder> settings)
    {
        final ValidationSettings validationSettings = validation.apply(ValidationSettings.builder().enabled(true)).build();
        final Settings allSettings = settings.apply(TestSettings.builder().outputFile(Path.of("front/models.generated.ts")))
                                             .validation(validationSettings)
                                             .build();
        return new Java2Ts(allSettings).generateValidation(classes);
    }

    private static String customer() {
        return generate(List.of(Customer.class), UnaryOperator.identity());
    }

    @Test
    void writesHeaderImportsAndTypedSchemas() {
        final String code = customer();
        assertAll(
            () -> assertTrue(code.startsWith("/* tslint:disable */"), code),
            () -> assertTrue(code.contains("import { z } from \"zod\";\nimport type { Customer, Status } from \"./models.generated\";\n"), code),
            () -> assertTrue(code.contains("export const CustomerSchema: z.ZodType<Customer> = z.object({\n"), code),
            () -> assertTrue(code.contains("export const StatusSchema = z.enum([\"ACTIVE\", \"INACTIVE\", \"removed\"]) satisfies z.ZodType<Status>;"), code),
            () -> assertFalse(code.contains("z.config"), code)
        );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        id: z.uuid(),
        name: z.string().regex(/\\S/, { error: "The name is required" }).max(50),
        email: z.email().nullish(),
        age: z.int().gte(18).lte(120),
        credit: z.number().gt(0).nullish(),
        code: z.string().regex(/^(?:[a-z]+\\/\\d+)$/i).nullish(),
        tags: z.array(z.string().regex(/\\S/)).min(1, { error: "At least 1 tag" }).nullish(),
        birthDate: z.iso.date().refine(v => v < localDate(), { error: "must be a past date" }).nullish(),
        expiresAt: z.iso.datetime().refine(v => Date.parse(v) >= Date.now(), { error: "must be a future date or the present" }).nullish(),
        createdAt: z.iso.datetime({ local: true }).nullish(),
        active: z.literal(true),
        balance: z.number().refine(v => /^\\d{0,5}(\\.\\d{0,2})?$/.test(String(Math.abs(v))), { error: "numeric value out of bounds (<5 digits>.<2 digits> expected)" }).nullish(),
        points: z.int().gt(0).nullish(),
        get status() { return StatusSchema; },
        get counters() { return z.partialRecord(StatusSchema, z.int()).nullish(); },
        names: z.record(z.number(), z.string()).nullish(),
        get partner() { return CustomerSchema.nullish(); },
        site: z.url().nullish(),
        initial: z.string().length(1),
        photo: z.base64().nullish(),
        nickname: z.string().nullish(),
        state: z.string().length(2).nullish(),
        rating: z.int().gte(1).lte(5),
        currency: z.string().length(3).nullish(),
        """)
    void convertsTypesAndConstraints(final String property) {
        final String code = customer();
        assertTrue(code.contains("    " + property + "\n"), code);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        asString | time: z.iso.time(),
        asString | offset: z.iso.datetime({ offset: true }),
        asString | year: z.string(),
        asString | legacy: z.iso.datetime({ offset: true }),
        asString | duration: z.string(),
        asString | uri: z.string(),
        asString | any: z.any(),
        asString | count: z.int().nullish(),
        asString | past: z.iso.datetime({ local: true }).refine(v => Date.parse(v) < Date.now(), { error: "must be a past date" }),
        asDate   | past: z.coerce.date().refine(v => v.getTime() < Date.now(), { error: "must be a past date" }),
        asDate   | future: z.coerce.date().refine(v => v.getTime() > Date.now(), { error: "must be a future date" }),
        """)
    void convertsDates(final DateMapping mapping, final String property) {
        final String code = generate(List.of(Dates.class), UnaryOperator.identity(), s -> s.mapDate(mapping));
        assertTrue(code.contains("    " + property + "\n"), code);
    }

    @Test
    void failsForTemporalConstraintsOnDatesAsNumbers() {
        final var ex = assertThrows(UnsupportedValidationException.class,
            () -> generate(List.of(Dates.class), UnaryOperator.identity(), s -> s.mapDate(DateMapping.asNumber)));
        assertTrue(String.valueOf(ex.getMessage()).contains("Dates.past: @Past: it doesn't apply to date/time LOCAL_DATE_TIME asNumber"), ex.getMessage());
    }

    @Test
    void writesLocalDateHelperOnlyWhenUsed() {
        assertTrue(customer().contains("const localDate = (): string => {"));
        assertFalse(generate(List.of(EmptyEnum.class), UnaryOperator.identity()).contains("localDate"));
    }

    @Test
    void writesNeverForEmptyEnum() {
        final String code = generate(List.of(EmptyEnum.class), UnaryOperator.identity());
        assertTrue(code.contains("export const EmptyEnumSchema = z.never() satisfies z.ZodType<EmptyEnum>;"), code);
    }

    @Test
    void writesFactoriesForGenericTypesAndBindsInheritedTypeVariables() {
        final String code = generate(List.of(CustomerPage.class), UnaryOperator.identity());
        assertAll(
            () -> assertTrue(code.contains("export const PageSchema = <T>(TSchema: z.ZodType<T>): z.ZodType<Page<T>> => z.object({\n    items: z.array(TSchema).nullish(),\n"), code),
            () -> assertTrue(code.contains("export const CustomerPageSchema: z.ZodType<CustomerPage> = z.object({\n    get items() { return z.array(CustomerSchema).nullish(); },\n    total: z.int(),\n    number: z.int(),\n"), code)
        );
    }

    @Test
    void usesFieldTypeAnnotationsAndImportsExcludedSchemas() {
        final String code = generate(List.of(Contact.class), v -> v.excludeClasses(List.of(ADDRESS)).customSchemasModule("./validation.custom"));
        assertAll(
            () -> assertTrue(code.contains("import { AddressSchema } from \"./validation.custom\";"), code),
            () -> assertTrue(code.contains("phones: z.array(z.string().regex(/\\S/)).nullish(),"), code),
            () -> assertTrue(code.contains("get address() { return AddressSchema.nullish(); },"), code),
            () -> assertFalse(code.contains("export const AddressSchema"), code)
        );
    }

    @Test
    void excludesClassesByPattern() {
        final String code = generate(List.of(Contact.class), v -> v.excludeClassPatterns(List.of(FIXTURES_PACKAGE + ".Addr*")).customSchemasModule("./custom"));
        assertTrue(code.contains("import { AddressSchema } from \"./custom\";"), code);
    }

    @Test
    void failsWhenExcludedSchemaIsReferencedWithoutCustomModule() {
        final var ex = assertThrows(UnsupportedValidationException.class, () -> generate(List.of(Contact.class), v -> v.excludeClasses(List.of(ADDRESS))));
        assertTrue(String.valueOf(ex.getMessage()).contains("Address"), ex.getMessage());
    }

    @Test
    void configuresLocaleWithoutDefaultMessages() {
        final String code = generate(List.of(Customer.class), v -> v.locale("pt-BR"));
        assertAll(
            () -> assertTrue(code.contains("import type { Customer, Status } from \"./models.generated\";\n\nz.config(z.locales.ptBR());\n\n"), code),
            () -> assertTrue(code.contains("birthDate: z.iso.date().refine(v => v < localDate()).nullish(),"), code)
        );
    }

    @Test
    void skipsZodConfiguration() {
        final String code = generate(List.of(Customer.class), v -> v.locale("pt-BR").zodConfig(false));
        assertFalse(code.contains("z.config"), code);
    }

    @Test
    void mirrorsTypeScriptOptionalityAndNullability() {
        final String code = generate(List.of(Address.class), UnaryOperator.identity(),
                                     s -> s.optionalPropertiesDeclaration(OptionalPropertiesDeclaration.nullableAndUndefinableType));
        assertAll(
            () -> assertTrue(code.contains("street: z.string(),"), code),
            () -> assertTrue(code.contains("number: z.int().nullable().or(z.undefined()),"), code),
            () -> assertTrue(code.contains("complements: z.array(z.string().nullable()).nullable().or(z.undefined()),"), code)
        );
    }

    @Test
    void usesCustomSchemaMappings() {
        final String code = generate(List.of(Customer.class), v -> v.customTypeMappings(Map.of("java.util.UUID", "UuidSchema")),
                                     s -> s.customTypeMappings(Map.of("java.util.UUID", "string")));
        assertTrue(code.contains("id: UuidSchema,"), code);
    }

    @Test
    void failsForConstraintsOnCustomSchemas() {
        final var ex = assertThrows(UnsupportedValidationException.class,
            () -> generate(List.of(Customer.class), v -> v.customTypeMappings(Map.of("java.math.BigDecimal", "DecimalSchema"))));
        assertTrue(String.valueOf(ex.getMessage()).contains("Customer.balance: @Digits: it doesn't apply to custom schema DecimalSchema"), ex.getMessage());
    }

    @Test
    void readsRepeatedConstraints() {
        final String code = generate(List.of(Odd.Repeated.class), UnaryOperator.identity());
        assertTrue(code.contains("value: z.string().regex(/^(?:[a-z]+)$/).regex(/^(?:a.*)$/).nullish(),"), code);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        CustomConstraint | CustomConstraint.value: @Even: constraint @Even isn't supported
        EmailOnNumber    | EmailOnNumber.value: @Email: it doesn't apply to integer
        ClassConstraint  | ClassConstraint: constraint @Even isn't supported
        JavaOnlyRegex    | JavaOnlyRegex.value: @Pattern: regular expression \\A[a-z]+ uses Java features
        """)
    void failsForUnsupportedConstraints(final String className, final String expectedMessage) throws ClassNotFoundException {
        final Class<?> aClass = Class.forName(Odd.class.getName() + "$" + className);
        final var ex = assertThrows(UnsupportedValidationException.class, () -> generate(List.of(aClass), UnaryOperator.identity()));
        assertTrue(String.valueOf(ex.getMessage()).contains(expectedMessage), ex.getMessage());
    }

    @Test
    void failsForTypeScriptMappingWithoutSchemaMapping() {
        final var ex = assertThrows(UnsupportedValidationException.class,
            () -> generate(List.of(Customer.class), UnaryOperator.identity(), s -> s.customTypeMappings(Map.of("java.math.BigDecimal", "string"))));
        assertTrue(String.valueOf(ex.getMessage()).contains("Customer.balance: type java.math.BigDecimal has a custom TypeScript mapping"), ex.getMessage());
    }

    @Test
    void writesValidationFileNextToModelsOnlyWhenEnabled(@TempDir final Path dir) throws IOException {
        final var context = new ClassPathContext(getClass().getClassLoader(), TestClasspath.entries());
        final var selection = ClassSelection.of(List.of(Customer.class.getName()), null, null, null, null, null, null);
        final SettingsBuilder builder = TestSettings.builder().outputFile(dir.resolve("models.ts")).classSelection(selection);

        new Java2Ts(builder.build()).generateFile(context);
        assertFalse(Files.exists(dir.resolve("validation.generated.ts")));

        new Java2Ts(builder.validation(ValidationSettings.builder().enabled(true).build()).build()).generateFile(context);
        final String code = Files.readString(dir.resolve("validation.generated.ts"));
        assertTrue(code.contains("from \"./models\";"), code);
    }
}
