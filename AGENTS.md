# AGENTS.md

Guidance for AI coding agents (and humans) working on this repository.

## Project overview

java2ts is a Maven plugin (and a plain Java API) that converts Java classes, records, enums and interfaces
into a single TypeScript file, optionally copying JavaDocs (extracted by the xml-doclet) as JSDoc comments.
It is a slim, single-module alternative to typescript-generator, focused only on model classes.

## Repository layout

| Path | Content |
|---|---|
| `java2ts/` | The plugin/library (the only module that is published). |
| `java2ts/src/test/java/.../fixtures` | Java classes converted by the tests. Their JavaDocs are used by the JavaDoc tests, so changing them may break tests. |
| `samples/sample/` | The base sample project: plain JPA entities, hand-written DTO records and validation annotations. The README examples come from it. |
| `samples/sample-lombok-dtogen/` | The same model using Lombok (`@Getter`/`@Setter`, `@Value`) and DTOGen (generated DTO records). |
| `samples/*/frontend/*.generated.ts` | The committed output of each sample (TypeScript types and Zod schemas). CI fails if they are out of date. |
| `samples/*/frontend/` | In each sample, a small npm project (Zod 4, TypeScript, tsx) whose `npm run check` type-checks the generated files and runs `validation.check.ts` against the schemas. |
| `.github/workflows/` | `build.yml` (build/test/sample check) and `deploy.yml` (Maven Central release on `v*.*.*` tags). |
| `.sdkmanrc` | JDK versions. Each `java=` line is a version in the CI build matrix. |

## Build and test

Requires JDK 25+ and Maven 3.9+.

```bash
mvn -f java2ts/pom.xml install    # compile, NullAway check, tests, JaCoCo check (>= 80% lines and branches)
mvn -f samples/sample/pom.xml compile                   # regenerates samples/sample/frontend/*.generated.ts (needs the plugin installed)
mvn -f samples/sample-lombok-dtogen/pom.xml compile     # the same for the Lombok/DTOGen sample
cd samples/sample/frontend && npm ci && npm run check   # tsc --strict over the generated files + runs the schemas (do it for both samples)
```

After changing how the output looks, rebuild both samples and commit the regenerated `*.generated.ts` files.
Keep the version of the sample poms equal to the plugin version, since the samples use `${project.version}` as the plugin version.

## Architecture

The flow is: select classes, then parse them into a TypeScript model, then write the model as code.
Everything is wired by the `Java2Ts` facade. The Maven `GenerateMojo` is just an adapter from pom parameters to `Settings`.

| Package | Responsibility |
|---|---|
| `config` | Immutable `Settings` (record), `ClassSelection`, `SettingsBuilder`, the setting enums and `Defaults`. |
| `scan` | Finds classes in the classpath (`ClasspathScanner`, `ClassPattern` globs, `ClassSelector`, `ExclusionFilter`). |
| `parser` | Converts classes into declarations: `ModelParser` (finds referenced classes), `InterfaceDeclarationParser`/`EnumDeclarationParser`, property extraction (`PropertyExtractor` strategies for records and beans), `PropertyResolver` (nullability/optionality), Jackson annotations and CLASS-retention annotations (ClassFile API). |
| `parser.type` | Converts Java types using a chain of generic `TypeMappingRule<R>`s built by `TypeMapperFactory`. The rules only classify Java types (`BasicKind`, `DateKind`, `MapKeyKind`) and call a `TypeRenderer<R>` (Abstract Factory) to build the result: `TsTypeRenderer` builds TypeScript types and `validation.SchemaTypeRenderer` builds schemas, so both outputs share the same classification. The rule order matters: `DeclaredTypeRule` must be the last one. |
| `ts` | The TypeScript model: sealed `TsType` and `TsDeclaration` hierarchies made of records, each knowing how to format itself. |
| `javadoc` | Reads xml-doclet XML using JAXB (`javadoc.xml` mapping classes) and runs the doclet in-process (`XmlDocletRunner`). |
| `writer` | Writes the file header and the model to the output file. |
| `validation` | Generates the validation file (`ValidationGenerator` facade): `ValidationModelParser` creates one schema per declared class (minus the excluded ones), `ConstraintReader` reads Bean Validation annotations by name (no compile dependency), `SchemaPropertyResolver` takes optionality/nullability from the TS property (so schemas and types always agree) and `TypeVariableBindings` resolves inherited type variables (object schemas include inherited properties). |
| `validation.model` | The library-agnostic schema model: sealed `SchemaType` and `Constraint` records. A constraint declares its `ConstraintTarget`s; applying it to a schema of another kind throws `UnsupportedValidationException`, which fails the build. |
| `validation.zod` | Writes the model as Zod 4 code (`ZodWriter`, `ZodTypeFormatter`, `ZodConstraintFormatter`, `ZodLocales`). |
| `maven` | The Mojo and the on-demand resolution of the xml-doclet from Maven repositories. |

To support a new Java type, create a `TypeMappingRule` and register it in `TypeMapperFactory` before `UndeclarableTypeRule`. If it needs a new kind of result, add a method to `TypeRenderer` and implement it in both renderers.

To support a new Bean Validation constraint, register a factory in `ConstraintReader`, create a `Constraint` record (declaring its `targets()`) and format it in `ZodConstraintFormatter` (the sealed hierarchy makes the switch fail to compile until you do).

## Conventions

- Java 25: use records, sealed interfaces, switch pattern matching, `var` and streams where they make the code clearer.
- Follow SOLID, one top-level class per file, small methods, constants instead of magic values, and immutable objects.
- Default values live only in `config.Defaults`. They are used by the `SettingsBuilder` and the `@Parameter(defaultValue=...)` of the Mojo. `GenerateMojoTest.createsSettingsWithDefaultValues` checks that they're in sync.
- Null safety: every package has a `package-info.java` with JSpecify `@NullMarked`. Use `@Nullable` only where null is really accepted. NullAway (Error Prone) checks this at compile time. Its JVM flags are in `java2ts/.mvn/jvm.config`.
- Document every public class and method with JavaDoc, including `@return` (or `{@return}`) and the effect of each `@param`.
- Tests use JUnit 6 and must keep the coverage at 80% or more for lines and branches (enforced by JaCoCo). Prefer end-to-end assertions over the generated TypeScript (`Java2TsTest`) plus focused unit tests.
- Keep the plugin slim: avoid new runtime dependencies. Jackson annotations are detected by their simple names, without depending on Jackson.
- Keep the setting names compatible with typescript-generator when a setting exists there.
- Update the README (configuration reference, conversion tables and the "What is not supported" list) when behavior changes.

## Gotchas

- The xml-doclet surrounds inline tags with commas (`,{@link X},`), repeats the tag name in tag texts (`@author X`), and includes type parameters in the names of generic classes (`Page<T>`). `CommentCleaner` and `TypeXml.getQualified()` fix these.
- The xml-doclet changes the thread context class loader. `XmlDocletRunner` restores it.
- Annotations with CLASS retention (such as `lombok.NonNull`) are not visible to reflection. They're read from class files by `ClassFileAnnotations`.
- The Mojo runs in the `compile` phase, after the compiler, so that `mvn compile` generates the file.
- Validation schemas must always match the generated TypeScript types: the sample CI runs `tsc --strict` over both files. When changing optionality/nullability rules, change `PropertyResolver` only, since `SchemaPropertyResolver` reads the result from the TS property.
- `maven-plugin-plugin` parses the sources with QDox, which fails ("could not match input") on some characters, such as literal U+2028/U+2029 inside char literals. Use numeric constants instead.
- JavaDoc extraction is on by default (`java2ts.javadoc`). Any extraction error (doclet download, javadoc tool) only logs a warning, so it never breaks the user's build.
- Classes are loaded in an isolated `URLClassLoader` with the platform class loader as parent, so plugin dependencies don't leak into the conversion.
