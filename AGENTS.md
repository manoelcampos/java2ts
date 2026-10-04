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
| `sample/` | A sample project using the plugin with Lombok, DTOGen, JPA and validation annotations. |
| `sample/frontend/models.generated.ts` | The committed output of the sample. CI fails if it is out of date. |
| `.github/workflows/` | `build.yml` (build/test/sample check) and `deploy.yml` (Maven Central release on `v*.*.*` tags). |
| `.sdkmanrc` | JDK versions. Each `java=` line is a version in the CI build matrix. |

## Build and test

Requires JDK 25+ and Maven 3.9+.

```bash
mvn -f java2ts/pom.xml install    # compile, NullAway check, tests, JaCoCo check (>= 80% lines and branches)
mvn -f sample/pom.xml compile     # regenerates sample/frontend/models.generated.ts (needs the plugin installed)
npx --package typescript tsc --noEmit --strict sample/frontend/models.generated.ts
```

After changing how the output looks, rebuild the sample and commit the regenerated `models.generated.ts`.
Keep the `sample/pom.xml` version equal to the plugin version, since the sample uses `${project.version}` as the plugin version.

## Architecture

The flow is: select classes, then parse them into a TypeScript model, then write the model as code.
Everything is wired by the `Java2Ts` facade. The Maven `GenerateMojo` is just an adapter from pom parameters to `Settings`.

| Package | Responsibility |
|---|---|
| `config` | Immutable `Settings` (record), `ClassSelection`, `SettingsBuilder`, the setting enums and `Defaults`. |
| `scan` | Finds classes in the classpath (`ClasspathScanner`, `ClassPattern` globs, `ClassSelector`, `ExclusionFilter`). |
| `parser` | Converts classes into declarations: `ModelParser` (finds referenced classes), `InterfaceDeclarationParser`/`EnumDeclarationParser`, property extraction (`PropertyExtractor` strategies for records and beans), `PropertyResolver` (nullability/optionality), Jackson annotations and CLASS-retention annotations (ClassFile API). |
| `parser.type` | Converts Java types using a chain of `TypeMappingRule`s built by `TypeMapperFactory`. The rule order matters: `DeclaredTypeRule` must be the last one. |
| `ts` | The TypeScript model: sealed `TsType` and `TsDeclaration` hierarchies made of records, each knowing how to format itself. |
| `javadoc` | Reads xml-doclet XML using JAXB (`javadoc.xml` mapping classes) and runs the doclet in-process (`XmlDocletRunner`). |
| `writer` | Writes the file header and the model to the output file. |
| `maven` | The Mojo and the on-demand resolution of the xml-doclet from Maven repositories. |

To support a new Java type, create a `TypeMappingRule` and register it in `TypeMapperFactory` before `UndeclarableTypeRule`.

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
- JavaDoc extraction is on by default (`java2ts.javadoc`). Any extraction error (doclet download, javadoc tool) only logs a warning, so it never breaks the user's build.
- Classes are loaded in an isolated `URLClassLoader` with the platform class loader as parent, so plugin dependencies don't leak into the conversion.
