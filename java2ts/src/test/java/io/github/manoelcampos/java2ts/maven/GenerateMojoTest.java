package io.github.manoelcampos.java2ts.maven;

import io.github.manoelcampos.java2ts.TestClasspath;
import io.github.manoelcampos.java2ts.config.OutputFileType;
import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.fixtures.Nullable;
import io.github.manoelcampos.java2ts.fixtures.Required;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.manoelcampos.java2ts.TestSettings.FIXTURES_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateMojoTest {
    @TempDir
    Path buildDir;
    private GenerateMojo mojo;

    @BeforeEach
    void setUp() {
        final var project = new MavenProject();
        project.setArtifactId("sample");
        final var build = new Build();
        build.setDirectory(buildDir.toString());
        build.setOutputDirectory(Path.of("target/test-classes").toAbsolutePath().toString());
        project.setBuild(build);
        project.addCompileSourceRoot(TestClasspath.FIXTURES_SOURCES.toAbsolutePath().toString());

        mojo = new GenerateMojo();
        mojo.project = project;
        mojo.classPatterns = List.of(FIXTURES_PACKAGE + ".Address", FIXTURES_PACKAGE + ".Outer");
        mojo.nullableAnnotations = List.of(Nullable.class.getName());
        mojo.requiredAnnotations = List.of(Required.class.getName());
        mojo.noFileDate = true;
        mojo.docletResolver = version -> TestClasspath.entries();
    }

    @Test
    void generatesDeclarationFileWithDefaultName() throws MojoExecutionException, IOException {
        mojo.outputFileType = OutputFileType.declarationFile;
        mojo.execute();
        final String content = Files.readString(buildDir.resolve("sample.d.ts"));
        assertTrue(content.contains("export interface Address {\n    street: string;"), content);
        assertTrue(content.contains("export interface Outer {"));
        assertFalse(content.contains("/**"));
    }

    @Test
    void generatesImplementationFileWithJavadocByDefault() throws MojoExecutionException, IOException {
        mojo.javadoc = true;
        mojo.customTypeMappings = List.of("java.lang.Integer:bigint");
        mojo.execute();

        final String content = Files.readString(buildDir.resolve("sample.ts"));
        assertTrue(content.contains("     * the street name\n"), content);
        assertTrue(content.contains("number?: bigint | null;"), content);
        assertTrue(Files.exists(buildDir.resolve("java2ts/javadoc.xml")));
    }

    @Test
    void generatesFileAtGivenPathUsingGivenJavadocFiles() throws MojoExecutionException, IOException {
        final Path file = buildDir.resolve("frontend/models.ts");
        mojo.outputFile = file.toFile();
        mojo.javadocXmlFiles = List.<File>of();
        mojo.execute();
        assertTrue(Files.exists(file));
    }

    @Test
    void skipsExecution() throws MojoExecutionException {
        mojo.skip = true;
        mojo.execute();
        assertFalse(Files.exists(buildDir.resolve("sample.d.ts")));
    }

    @Test
    void wrapsErrors() {
        mojo.classes = List.of("com.inexistent.Foo");
        final var ex = assertThrows(MojoExecutionException.class, mojo::execute);
        assertTrue(String.valueOf(ex.getMessage()).contains("com.inexistent.Foo"));
    }

    @Test
    void createsSettingsWithDefaultValues() {
        final var defaultMojo = new GenerateMojo();
        defaultMojo.project = mojo.project;
        final var settings = defaultMojo.settings(List.of());
        final var expected = Settings.builder().outputFile(settings.outputFile()).build();
        assertEquals(expected, settings, "The plugin default values must be the same as the SettingsBuilder ones");
        assertEquals(buildDir.resolve("sample.ts"), settings.outputFile());
    }

    @Test
    void usesEmptyAnnotationListsWhenGiven() {
        mojo.nullableAnnotations = List.of();
        mojo.requiredAnnotations = List.of();
        final var settings = mojo.settings(List.of());
        assertTrue(settings.nullableAnnotations().isEmpty());
        assertTrue(settings.requiredAnnotations().isEmpty());
    }

    @Test
    void failsToResolveDocletWithoutRepositorySystem() {
        mojo.docletResolver = null;
        mojo.javadoc = true;
        assertThrows(MojoExecutionException.class, mojo::execute);
    }
}
