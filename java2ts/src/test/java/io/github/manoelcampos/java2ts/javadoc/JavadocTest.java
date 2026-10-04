package io.github.manoelcampos.java2ts.javadoc;

import io.github.manoelcampos.java2ts.Java2Ts;
import io.github.manoelcampos.java2ts.TestClasspath;
import io.github.manoelcampos.java2ts.TestSettings;
import io.github.manoelcampos.java2ts.fixtures.Address;
import io.github.manoelcampos.java2ts.fixtures.BaseModel;
import io.github.manoelcampos.java2ts.fixtures.Outer;
import io.github.manoelcampos.java2ts.fixtures.Person;
import io.github.manoelcampos.java2ts.fixtures.Status;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the extraction of JavaDocs from the fixture sources using the xml-doclet,
 * and their inclusion into the generated TypeScript code.
 */
class JavadocTest {
    @TempDir
    static Path tempDir;
    private static Path xmlFile;
    private static Javadoc javadoc;

    @BeforeAll
    static void extractJavadoc() {
        final var runner = new XmlDocletRunner(TestClasspath.entries());
        xmlFile = runner.run(List.of(TestClasspath.FIXTURES_SOURCES), TestClasspath.entries(), tempDir.resolve("docs/javadoc.xml")).orElseThrow();
        javadoc = XmlJavadoc.load(List.of(xmlFile));
    }

    @Test
    void readsClassComments() {
        assertEquals("Base interface for all models.\n@author Manoel Campos", javadoc.classComment(BaseModel.class));
        assertEquals("A nested class.", javadoc.classComment(Outer.Inner.class));
        assertEquals("The status of a person.", javadoc.classComment(Status.class));
        assertEquals("A generic page of items.", javadoc.classComment(io.github.manoelcampos.java2ts.fixtures.Page.class));
        assertEquals("", javadoc.classComment(new Object(){}.getClass()));
    }

    @Test
    void readsMemberComments() throws ReflectiveOperationException {
        assertEquals("The person's name.", javadoc.memberComment(Person.class.getDeclaredField("name")));
        assertEquals("Returns the model id", javadoc.memberComment(BaseModel.class.getMethod("getId")));
        assertEquals("A getter without backing field.", javadoc.memberComment(Person.class.getMethod("getFullDescription")));
        assertEquals("the street name", javadoc.memberComment(Address.class.getRecordComponents()[0]));
        assertEquals("", javadoc.memberComment(Person.class.getMethod("getAge")));
        assertEquals("", javadoc.memberComment((AnnotatedElement) Person.class));
    }

    @Test
    void fixesInlineTags() {
        assertEquals("The id inherited from {@link BaseModel}.", javadoc.memberComment(field("id")));
    }

    private static java.lang.reflect.Field field(final String name) {
        try {
            return io.github.manoelcampos.java2ts.fixtures.AbstractModel.class.getDeclaredField(name);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void copiesJavadocToTypeScript() {
        final var settings = TestSettings.builder().javadocXmlFiles(List.of(xmlFile)).build();
        final String output = new Java2Ts(settings).generate(List.of(Person.class, Address.class));
        assertTrue(output.contains("/**\n * Base interface for all models.\n * @author Manoel Campos\n */\nexport interface BaseModel {\n    /**\n     * Returns the model id\n     */\n    id?: number | null;"), output);
        assertTrue(output.contains("    /**\n     * The person's name.\n     */\n    name: string;"), output);
        assertTrue(output.contains("    /**\n     * the street name\n     */\n    street: string;"), output);
    }

    @Test
    void returnsEmptyWhenThereAreNoSources(@TempDir final Path emptyDir) {
        final var runner = new XmlDocletRunner(TestClasspath.entries());
        assertTrue(runner.run(List.of(emptyDir, emptyDir.resolve("missing")), List.of(), emptyDir.resolve("javadoc.xml")).isEmpty());
    }

    @Test
    void failsWhenSourcesHaveErrors(@TempDir final Path dir) throws java.io.IOException {
        java.nio.file.Files.writeString(dir.resolve("Broken.java"), "public class Broken { invalid }");
        final var runner = new XmlDocletRunner(TestClasspath.entries());
        final List<Path> roots = List.of(dir);
        final Path output = dir.resolve("javadoc.xml");
        final var ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> runner.run(roots, List.of(), output));
        assertTrue(String.valueOf(ex.getMessage()).contains("Error extracting JavaDocs"));
    }

    @Test
    void returnsNoneWhenThereAreNoXmlFiles() {
        assertEquals(Javadoc.NONE, XmlJavadoc.load(List.of()));
        assertEquals("", Javadoc.NONE.classComment(Person.class));
        assertEquals("", Javadoc.NONE.memberComment(Person.class));
    }
}
