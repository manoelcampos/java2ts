# java2ts: Java to TypeScript generator [![Build Status](https://github.com/manoelcampos/java2ts/actions/workflows/build.yml/badge.svg)](https://github.com/manoelcampos/java2ts/actions/workflows/build.yml) [![Maven Central](https://img.shields.io/maven-central/v/io.github.manoelcampos/java2ts.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=java2ts&namespace=io.github.manoelcampos) [![javadoc](https://javadoc.io/badge2/io.github.manoelcampos/java2ts/javadoc.svg)](https://javadoc.io/doc/io.github.manoelcampos/java2ts)

java2ts is a slim Java 25+ Maven plugin that converts your Java model classes into a single TypeScript file, so that the frontend always uses the same types as the backend.

It converts POJOs/Java Beans, JPA/Hibernate entities, records, DTOs, enums, interfaces, abstract classes and generic types. It can also **copy the JavaDocs** into the TypeScript file as JSDoc comments: you just set one pom property.

It's inspired by the [typescript-generator](https://github.com/vojtechhabarta/typescript-generator) and uses the same names for its settings. But it focuses only on converting model classes, with a single module, very few dependencies and reasonable defaults.

## Table of contents

1. [Features](#1-features)
2. [Quick start](#2-quick-start)
3. [How Java types are converted](#3-how-java-types-are-converted)
4. [Required, optional and nullable properties](#4-required-optional-and-nullable-properties)
5. [Copying JavaDocs to TypeScript](#5-copying-javadocs-to-typescript)
6. [Configuration reference](#6-configuration-reference)
7. [Selecting classes](#7-selecting-classes)
8. [Custom type mappings](#8-custom-type-mappings)
9. [Using java2ts as a library](#9-using-java2ts-as-a-library)
10. [Migrating from typescript-generator](#10-migrating-from-typescript-generator)
11. [What is not supported](#11-what-is-not-supported)
12. [Building and contributing](#12-building-and-contributing)

## 1. Features

- Converts classes, abstract classes, interfaces, records, enums, nested classes and generic types (including bounded type parameters such as `T extends Number`).
- Supports nested generics, such as `Map<String, List<Map<Integer, Address>>>`, both in conversions and in [custom type mappings](#8-custom-type-mappings).
- Works with Lombok (properties come from the getters it generates) and [DTOGen](https://github.com/manoelcampos/dtogen) (DTO records are converted too).
- Follows the Jackson rules to find properties, and handles `@JsonIgnore` and `@JsonProperty` (Jackson 2 or 3) without depending on Jackson.
- Ignores JDK interfaces that aren't useful for the frontend, such as `Serializable`, `Comparable` and `Cloneable`.
- Understands `@Nullable`/`@NotNull`-like annotations, including annotations with CLASS retention such as `lombok.NonNull` (read from class files using the Java 24+ [ClassFile API](https://openjdk.org/jeps/484)).
- Type-use annotations work at any level, such as `List<@Nullable String>`, which becomes `(string | null)[]`.
- `Optional<T>` properties become optional TypeScript properties (`name?: T`).
- Maps become `Record<K, V>`, and maps with enum keys become `Partial<Record<Enum, V>>`.
- `@Deprecated` classes and properties get a JSDoc `@deprecated` tag, so editors show them as deprecated.
- Copies JavaDocs from classes, fields, getters and record components to the generated file, using the [xml-doclet](https://github.com/manticore-projects/xml-doclet). The doclet is only downloaded when this feature is on.
- Generates a single, deterministic file: declarations are sorted and, by default, the header has no date. The file only changes when your classes change.
- Has no runtime dependencies besides JAXB, which reads the JavaDoc XML.

## 2. Quick start

Add the plugin to your `pom.xml`. The only required setting is which classes to convert:

```xml
<plugin>
    <groupId>io.github.manoelcampos</groupId>
    <artifactId>java2ts</artifactId>
    <!-- Set a specific version or use the latest one -->
    <version>JAVA2TS_VERSION_HERE</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <outputFile>../frontend/src/models.generated.ts</outputFile>
        <classPatterns>
            <classPattern>com.company.app.model.**</classPattern>
            <classPattern>com.company.app.dto.**</classPattern>
        </classPatterns>
    </configuration>
</plugin>
```

To also copy the JavaDocs, add this property:

```xml
<properties>
    <java2ts.javadoc>true</java2ts.javadoc>
</properties>
```

Then compile the project:

```bash
mvn compile
```

The plugin runs in the `compile` phase, right after your classes are compiled. You can also run just the plugin with `mvn compile java2ts:generate`.

### 2.1 Example

The [sample project](sample) has JPA entities with Lombok, a DTOGen DTO, records, enums and generic types. For instance, this entity:

```java
/**
 * A person, which is a JPA entity whose getters and setters are generated by Lombok.
 */
@Entity @Getter @Setter @DTO
public class Person extends AbstractBaseModel implements Comparable<Person> {
    /** The person's full name. */
    @NotNull @NotBlank
    private String name;

    /** The e-mail, which is optional. */
    @Email
    private String email;

    @NotNull
    private LocalDate birthDate;

    @ManyToOne @Nullable @DTO.MapToId
    private Person partner;

    @NotNull @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @ElementCollection @NotNull
    private List<Phone> phones = new ArrayList<>();

    private int loginAttempts;

    @JsonIgnore @DTO.Exclude
    private String password;

    public Optional<String> getNickname() { ... }
    public Map<Status, Integer> getLoginsByStatus() { ... }
    // ...
}
```

is converted to this TypeScript code (some JavaDocs were removed here):

```typescript
/**
 * A person, which is a JPA entity whose getters and setters are generated by Lombok.
 */
export interface Person extends AbstractBaseModel {
    /**
     * The person's full name.
     */
    name: string;
    /**
     * The e-mail, which is optional.
     */
    email?: string | null;
    birthDate: string;
    partner?: Person | null;
    status: Status;
    phones: Phone[];
    loginAttempts: number;
    loginsByStatus?: Partial<Record<Status, number>> | null;
    nickname?: string | null;
}

export interface PersonDTO extends DTORecord<Person> {
    id?: number | null;
    name: string;
    email?: string | null;
    birthDate: string;
    countryId: number;
    partnerId?: number | null;
    status: Status;
    phones: Phone[];
    loginAttempts: number;
}

export type Status = "ACTIVE" | "INACTIVE" | "BLOCKED";
```

Note that:
- `Comparable` was ignored, and `password` was ignored because of `@JsonIgnore`;
- the `id` property is declared just once, in the `BaseModel` interface that `AbstractBaseModel` extends;
- `loginAttempts` is required because it is a primitive;
- `nickname` is optional because it's a Java `Optional`.

Check the full generated file at [sample/frontend/models.generated.ts](sample/frontend/models.generated.ts).

Every generated file starts with:

```typescript
/* tslint:disable */
/* eslint-disable */
// This file is generated by java2ts and must not be manually changed.
```

## 3. How Java types are converted

| Java type | TypeScript type |
|---|---|
| `String`, `char`, `Character`, `CharSequence`, `UUID`, `URI`, `URL` | `string` |
| `byte`, `short`, `int`, `long`, `float`, `double`, their wrappers, `BigDecimal`, `BigInteger` and any other `Number` | `number` |
| `boolean`, `Boolean` | `boolean` |
| `byte[]` (serialized as Base64) | `string` |
| `Duration`, `Period`, `ZoneId` (serialized as ISO-8601) | `string` |
| `Date`, `Calendar`, `LocalDate`, `LocalDateTime`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime` and any other `Temporal` | `string` by default ([`mapDate`](#6-configuration-reference) can change it to `Date` or `number`) |
| Arrays and any `Iterable` (`List`, `Set`, `Collection`, ...) of `T` | `T[]` |
| `Map<String, V>` (or any key that is not a number or enum) | `Record<string, V>` |
| `Map<Integer, V>` (any numeric key) | `Record<number, V>` |
| `Map<MyEnum, V>` | `Partial<Record<MyEnum, V>>` |
| `Optional<T>` | `T`, and the property becomes optional (`name?: T`) |
| `OptionalInt`, `OptionalLong`, `OptionalDouble` | `number`, and the property becomes optional |
| `Object`, `?`, `? super T` and JDK types not listed here | `any` |
| `? extends T` | `T` |
| Type variables such as `T` | `T` |
| Enums | string literal union types, such as `export type Color = "RED" \| "BLUE";` |
| Classes, abstract classes, interfaces and records | `export interface` declarations |

Some more rules:
- Raw types get `any` as type arguments, such as `Page<any>` for a raw `Page`.
- A type parameter's bounds are kept when they are declared in TypeScript. For instance, `class Box<N extends Number, P extends Page<N> & Pet>` becomes `interface Box<N extends number, P extends Page<N> & Pet>`. Bounds that become `any` (such as `T extends Comparable<T>`) are dropped.
- Classes referenced by the converted ones (supertypes, property types and type arguments) are converted too, even if your [class selection](#7-selecting-classes) doesn't include them. For instance, `DTORecord<T>` from DTOGen.
- A TypeScript interface extends the interfaces generated for its Java superclass and interfaces. JDK supertypes (such as `Serializable`, `Comparable` and `Record`) and excluded classes are left out. Each interface only declares the properties it doesn't inherit.
- Nested classes use their simple names (`Outer.Inner` becomes `Inner`). If two classes have the same simple name, the generation fails with a message asking you to exclude one of them.
- Declarations are sorted: interfaces first, then type aliases, both in alphabetical order.

### 3.1 Which properties are included

Properties are found the same way Jackson finds them:

- **Records**: each component is a property.
- **Classes and interfaces**: each public getter is a property (`getX()`, or `isX()` returning `boolean`). Getters generated by Lombok count too. Public fields and fields annotated with `@JsonProperty` are also included. Static members and `getClass()` are ignored.
- Properties annotated with `@JsonIgnore` (on the field, getter or record component) are ignored.
- `@JsonProperty("newName")` renames a property or an enum constant. Names that aren't valid identifiers are quoted, such as `"e-mail"?: string;`.
- Properties keep the order in which their fields are declared. Properties without a field (getter-only) come at the end, in alphabetical order.

Annotations and JavaDocs of a property are read from its field, its getters and its record component. So annotations on a Lombok field work as if they were on the getter.

## 4. Required, optional and nullable properties

These two concepts are independent:

| Concept | Defined by | TypeScript (with the default settings) |
|---|---|---|
| **Nullable**: the value may be null | an annotation in [`nullableAnnotations`](#6-configuration-reference) | `type \| null` |
| **Optional**: the property may be absent | no annotation from [`requiredAnnotations`](#6-configuration-reference), or a Java `Optional` type | `name?: type \| null` |

The rules are:

1. If `requiredAnnotations` is not empty, a property without any of these annotations is **optional**.
   The defaults are `@NotNull`, `@NotBlank` and `@NotEmpty` (Jakarta and javax validation), JSpecify `@NonNull`, JetBrains `@NotNull` and Lombok `@NonNull`.
2. **Primitive** properties (`int`, `boolean`, ...) are always required, since they can't be null.
3. Properties whose type is `Optional`, `OptionalInt`, `OptionalLong` or `OptionalDouble` are always optional.
4. A property (or a type usage, such as in `List<@Nullable String>`) with any of the `nullableAnnotations` is **nullable**.
   The defaults are the `@Nullable` annotations from JSpecify, JetBrains, `jakarta.annotation` and `javax.annotation`.

The settings below define how optional and nullable properties are written.

**`optionalPropertiesDeclaration`**: how optional properties are declared

| Value | Example |
|---|---|
| `questionMark` | `name?: string;` |
| `questionMarkAndNullableType` (default) | `name?: string \| null;` |
| `nullableType` | `name: string \| null;` |
| `nullableAndUndefinableType` | `name: string \| null \| undefined;` |
| `undefinableType` | `name: string \| undefined;` |

The default `questionMarkAndNullableType` fits Jackson, which writes null properties as `null` by default (instead of leaving them out).

**`nullabilityDefinition`**: how nullable types are written

| Value | Example |
|---|---|
| `nullInlineUnion` (default) | `string \| null` |
| `undefinedInlineUnion` | `string \| undefined` |
| `nullAndUndefinedInlineUnion` | `string \| null \| undefined` |
| `nullUnion` | `Nullable<string>`, plus `export type Nullable<T> = T \| null;` |
| `undefinedUnion` | `Nullable<string>`, plus `export type Nullable<T> = T \| undefined;` |
| `nullAndUndefinedUnion` | `Nullable<string>`, plus `export type Nullable<T> = T \| null \| undefined;` |

The `Nullable<T>` alias is only declared when it's used.

## 5. Copying JavaDocs to TypeScript

To copy the JavaDocs of your classes into the TypeScript file as JSDoc comments, set the `java2ts.javadoc` property:

```xml
<properties>
    <java2ts.javadoc>true</java2ts.javadoc>
</properties>
```

or use `<javadoc>true</javadoc>` inside the plugin `<configuration>`, or run `mvn compile -Djava2ts.javadoc=true`.

When it's on, the plugin:
1. downloads the [xml-doclet](https://github.com/manticore-projects/xml-doclet) (and its dependencies) from Maven Central, just like any other dependency. The version is set by the `xmlDocletVersion` parameter;
2. runs the JDK javadoc tool with the xml-doclet over all the project source directories, including the ones with generated sources, such as DTOGen DTOs. The result is the `target/java2ts/javadoc.xml` file;
3. copies the docs to the TypeScript declarations:
   - class docs (including tags such as `@author`, but not `@param`) go to interfaces and enum types;
   - field docs go to properties. If a field has no docs, the getter docs are used, or else its `@return` tag;
   - the `@param` tags of a record's docs go to the record properties.

`{@code x}` and `{@literal x}` become `` `x` ``, and `{@return x}` becomes `Returns x`. Other inline tags, such as `{@link Foo}`, are kept, since JSDoc supports them.

If you already have XML files generated by the xml-doclet, list them in `javadocXmlFiles` instead.

## 6. Configuration reference

All parameters can be set inside the plugin `<configuration>`. The ones with a property can also be set using `<properties>` in the pom or `-Dproperty=value` in the command line.

| Parameter | Property | Default | Description |
|---|---|---|---|
| `outputFile` | `java2ts.outputFile` | `target/${artifactId}.ts` (`.d.ts` for `declarationFile`) | The TypeScript file to generate. Parent directories are created if needed. |
| `outputFileType` | `java2ts.outputFileType` | `implementationFile` | `implementationFile` (`.ts`) or `declarationFile` (`.d.ts`). Both export all declarations. |
| `classes` | | | Fully qualified names of classes to convert. |
| `classPatterns` | | | Glob patterns of classes to convert. See [Selecting classes](#7-selecting-classes). |
| `classesWithAnnotations` | | | Converts the project classes with any of these annotations. |
| `classesImplementingInterfaces` | | | Converts the project classes implementing any of these interfaces. |
| `classesExtendingClasses` | | | Converts the project classes extending any of these classes. |
| `excludeClasses` | | | Fully qualified names of classes that must not be converted. References to them become `any`. |
| `excludeClassPatterns` | | | Glob patterns of classes that must not be converted. |
| `nullableAnnotations` | | JSpecify, JetBrains, `jakarta.annotation` and `javax.annotation` `@Nullable` | Annotations that make properties and types nullable. |
| `requiredAnnotations` | | Jakarta/javax `@NotNull`, `@NotBlank` and `@NotEmpty`, JSpecify `@NonNull`, JetBrains `@NotNull`, Lombok `@NonNull` | Annotations that make properties required. If not empty, properties without them are optional. Use an empty list (`<requiredAnnotations/>`) to make all properties required. |
| `optionalPropertiesDeclaration` | `java2ts.optionalPropertiesDeclaration` | `questionMarkAndNullableType` | How optional properties are declared. See [section 4](#4-required-optional-and-nullable-properties). |
| `nullabilityDefinition` | `java2ts.nullabilityDefinition` | `nullInlineUnion` | How nullable types are written. See [section 4](#4-required-optional-and-nullable-properties). |
| `mapDate` | `java2ts.mapDate` | `asString` | How date/time types are converted: `asString`, `asDate` or `asNumber`. |
| `customTypeMappings` | | | Mappings in the format `javaType:tsType`. See [Custom type mappings](#8-custom-type-mappings). |
| `noFileDate` | `java2ts.noFileDate` | `true` | If `false`, the comment at the beginning of the file includes the generation date. The comment saying that the file is generated is always included. |
| `javadoc` | `java2ts.javadoc` | `false` | Extracts the project JavaDocs and copies them to the TypeScript file. See [section 5](#5-copying-javadocs-to-typescript). |
| `javadocXmlFiles` | | | XML files generated by the xml-doclet, from where JavaDocs are copied. |
| `xmlDocletVersion` | `java2ts.xmlDocletVersion` | `2.0.3` | The xml-doclet version used when `javadoc` is `true`. |
| `skip` | `java2ts.skip` | `false` | Skips the plugin execution. |

A complete configuration example:

```xml
<configuration>
    <outputFile>${project.basedir}/../frontend/app/model/models.generated.ts</outputFile>
    <outputFileType>implementationFile</outputFileType>
    <classPatterns>
        <classPattern>com.company.app.model.**</classPattern>
        <classPattern>com.company.app.dto.*DTO</classPattern>
    </classPatterns>
    <excludeClassPatterns>
        <excludeClassPattern>**.internal.**</excludeClassPattern>
    </excludeClassPatterns>
    <nullableAnnotations>
        <nullableAnnotation>org.jetbrains.annotations.Nullable</nullableAnnotation>
    </nullableAnnotations>
    <requiredAnnotations>
        <requiredAnnotation>jakarta.validation.constraints.NotNull</requiredAnnotation>
        <requiredAnnotation>jakarta.validation.constraints.NotBlank</requiredAnnotation>
        <requiredAnnotation>lombok.NonNull</requiredAnnotation>
    </requiredAnnotations>
    <optionalPropertiesDeclaration>questionMarkAndNullableType</optionalPropertiesDeclaration>
    <nullabilityDefinition>nullInlineUnion</nullabilityDefinition>
    <mapDate>asString</mapDate>
    <customTypeMappings>
        <mapping>java.math.BigDecimal:string</mapping>
    </customTypeMappings>
    <noFileDate>true</noFileDate>
    <javadoc>true</javadoc>
</configuration>
```

## 7. Selecting classes

You can combine any of the selection parameters. The selected classes are the union of:
- the classes listed in `classes`;
- the classes whose names match any of the `classPatterns`. All classpath entries (project classes and dependency jars) are searched;
- the project's own compiled classes (not the dependency jars) that have any of the `classesWithAnnotations`, implement any of the `classesImplementingInterfaces` or extend any of the `classesExtendingClasses`.

Then `excludeClasses` and `excludeClassPatterns` are removed. Annotations, anonymous, local and synthetic classes are never converted.

Patterns are globs over fully qualified class names:

| Pattern | Matches | Doesn't match |
|---|---|---|
| `com.app.model.**` | `com.app.model.Person`, `com.app.model.sub.Address`, `com.app.model.Person$Inner` | `com.app.other.Person` |
| `com.app.model.*` | `com.app.model.Person` | `com.app.model.sub.Address`, `com.app.model.Person$Inner` |
| `com.app.*.dto.*DTO` | `com.app.billing.dto.InvoiceDTO` | `com.app.billing.dto.Invoice` |

`**` matches any chars, while `*` matches any chars inside a single package or class name (it doesn't match `.` nor the `$` of nested classes).

## 8. Custom type mappings

Custom mappings replace the conversion of specific Java types. Each mapping has the format `javaType:tsType`:

```xml
<customTypeMappings>
    <!-- Keeps the precision of BigDecimal values serialized as strings -->
    <mapping>java.math.BigDecimal:string</mapping>
    <!-- Square brackets can be used instead of &lt; and &gt; for generic type arguments -->
    <mapping>java.util.List[java.util.List[java.lang.Double]]:[number, number][]</mapping>
    <mapping>java.util.Map[java.lang.String, java.util.List[java.lang.Long]]:Record&lt;string, bigint[]&gt;</mapping>
</customTypeMappings>
```

Mappings support generic type arguments, even nested ones. This follows the fixes from the typescript-generator [PR #1154](https://github.com/vojtechhabarta/typescript-generator/pull/1154):
- A mapping **with** type arguments only matches that exact type. `java.util.List[java.math.BigDecimal]:number[]` changes `List<BigDecimal>`, but not `List<String>` nor a raw `List`.
- A mapping **without** type arguments (such as `java.util.List:any[]`) matches the type with any type arguments.
- Nested type arguments are parsed correctly. For instance, `Class[T1[T2], T3]` has the arguments `T1<T2>` and `T3`, not `T1` and `T3`.
- You can use `<` and `>` (written as `&lt;` and `&gt;` in the pom) or `[` and `]`. Spaces are ignored, and nested classes may use `.` or `$` (`com.app.Outer.Inner` or `com.app.Outer$Inner`).

The TypeScript side is written as given. If it refers to a type that isn't declared in the generated file, the file won't compile. So prefer built-in types (such as tuples `[number, number]`, `Record<K, V>` or `bigint`).

## 9. Using java2ts as a library

The plugin is a thin layer over a plain Java API, which you can use in tests or build scripts:

```java
var settings = Settings.builder()
                       .outputFile(Path.of("frontend/models.generated.ts"))
                       .classPatterns("com.company.app.model.**")
                       .mapDate(DateMapping.asString)
                       .build();

var java2ts = new Java2Ts(settings);

// Writes the file with the classes selected from the current classpath
java2ts.generateFile(ClassPathContext.ofCurrentClassPath());

// Or just gets the TypeScript code for some classes
String code = java2ts.generate(List.of(Person.class, Address.class));
```

To extract JavaDocs programmatically, run the `XmlDocletRunner` with the xml-doclet jars and pass the generated XML file to `SettingsBuilder.javadocXmlFiles()`.

## 10. Migrating from typescript-generator

The settings in this README have the same names and values as in typescript-generator. Replace the plugin coordinates and check these differences:

| typescript-generator | java2ts |
|---|---|
| `jsonLibrary` | Not needed. The Jackson rules are always used. |
| `outputKind` | Not needed. The file is always a module that exports all declarations. |
| `noFileComment` | Removed. The comment saying that the file is generated is always included. Use `noFileDate` to remove only the date (it's removed by default). |
| `javadocXmlFiles` with a separate maven-javadoc-plugin execution | Just set `java2ts.javadoc=true`. |
| `optionalAnnotations`, `primitivePropertiesRequired` | Use `requiredAnnotations`. Primitives are always required. |
| `mapDate` default `asDate` | Default `asString`, which is how Jackson writes `java.time` types. Set `asDate` to keep the old behavior. |
| `Map<K, V>` as `{ [index: string]: V }` | `Record<string, V>` (or `Record<number, V>` / `Partial<Record<Enum, V>>`), which is equivalent. |
| `Serializable`, `Comparable` and other JDK interfaces declared as empty interfaces | Ignored. |
| Annotations with CLASS retention (such as `lombok.NonNull`) are invisible | Read from class files. |
| Declarations of referenced classes appended in discovery order | All declarations sorted by name. |

For instance, the saneaplan configuration below:

```xml
<plugin>
    <groupId>cz.habarta.typescript-generator</groupId>
    <artifactId>typescript-generator-maven-plugin</artifactId>
    <configuration>
        <jsonLibrary>jackson2</jsonLibrary>
        <outputFileType>implementationFile</outputFileType>
        <outputFile>../frontend/app/model/models.generated.ts</outputFile>
        <nullabilityDefinition>nullInlineUnion</nullabilityDefinition>
        <optionalPropertiesDeclaration>questionMarkAndNullableType</optionalPropertiesDeclaration>
        <!-- ... annotations ... -->
        <classPatterns>
            <class>app.model.**</class>
            <class>app.dto.**</class>
        </classPatterns>
        <outputKind>module</outputKind>
        <noFileComment>true</noFileComment>
    </configuration>
</plugin>
```

becomes:

```xml
<plugin>
    <groupId>io.github.manoelcampos</groupId>
    <artifactId>java2ts</artifactId>
    <configuration>
        <outputFile>../frontend/app/model/models.generated.ts</outputFile>
        <!-- ... annotations (or just use the defaults) ... -->
        <classPatterns>
            <class>app.model.**</class>
            <class>app.dto.**</class>
        </classPatterns>
        <javadoc>true</javadoc>
    </configuration>
</plugin>
```

## 11. What is not supported

java2ts focuses on converting model classes. These features are not supported:

- **REST clients and services**: JAX-RS and Spring controllers are not converted into client code.
- **Polymorphism**: `@JsonTypeInfo`/`@JsonSubTypes` and sealed hierarchies don't produce discriminated unions. Subtypes are plain interfaces that extend their supertypes.
- **Other Jackson features**: only `@JsonIgnore` and `@JsonProperty` are handled. These are not: `@JsonValue`, `@JsonCreator`, `@JsonUnwrapped`, `@JsonIgnoreProperties`, `@JsonInclude`, `@JsonFormat`, `@JsonNaming` and naming strategies, mix-ins, and custom serializers.
- **Other JSON libraries**: Gson and JSON-B annotations are ignored.
- **Other output styles**: no TypeScript `enum`s or `const` objects (enums are always string literal unions), no classes, no `readonly` properties, no namespaces/global declarations, and no splitting into multiple files.
- **Big numbers**: `long`, `BigInteger` and `BigDecimal` become `number`, which may lose precision in JavaScript. Use a [custom type mapping](#8-custom-type-mappings) (such as `java.lang.Long:bigint` or `java.math.BigDecimal:string`) if your JSON writes them differently.
- **Custom mappings with type variables**, such as `com.app.Wrapper<T>:T[]`. Only concrete types and raw types can be mapped.
- **Map keys** other than strings, numbers and enums are converted to `string`.
- **Classes with the same simple name** in different packages can't be converted together. Exclude one of them.
- **JavaDocs of enum constants** are not copied, since union types can't document their members. Inline tags other than `{@code}`, `{@literal}` and `{@return}` are kept as written.
- **Selecting classes by annotation or supertype from dependency jars**: only the project's own classes are searched. Use `classes` or `classPatterns` for classes in jars.
- **Kotlin and Scala** specific features, such as Kotlin nullability.
- **Gradle**: only a Maven plugin is provided. The Java API can be called from other build tools.

## 12. Building and contributing

You need JDK 25 or newer (check [.sdkmanrc](.sdkmanrc)) and Maven 3.9+.

```bash
# Builds and tests the plugin (the build fails with less than 80% of line/branch coverage)
mvn -f java2ts/pom.xml install

# Builds the sample project, which regenerates sample/frontend/models.generated.ts
mvn -f sample/pom.xml compile
```

The project uses [JSpecify](https://jspecify.dev) annotations, checked at compile time by [NullAway](https://github.com/uber/NullAway). Check [AGENTS.md](AGENTS.md) for the project architecture and conventions.

Releases are published to Maven Central when a `vX.Y.Z` tag is pushed (see [deploy.yml](.github/workflows/deploy.yml)).

## License

This project is licensed under the [GPLv3](LICENSE).
