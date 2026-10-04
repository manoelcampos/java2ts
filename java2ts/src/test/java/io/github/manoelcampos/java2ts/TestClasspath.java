package io.github.manoelcampos.java2ts;

import io.github.manoelcampos.java2ts.scan.ClassPathContext;

import java.nio.file.Path;
import java.util.List;

/**
 * Paths used by tests.
 */
public final class TestClasspath {
    /** The directory of the fixture sources, used to extract JavaDocs. */
    public static final Path FIXTURES_SOURCES = Path.of("src/test/java/io/github/manoelcampos/java2ts/fixtures");

    private TestClasspath() {/**/}

    /**
     * {@return the test classpath entries, which include the xml-doclet and its dependencies}
     */
    public static List<Path> entries() {
        return ClassPathContext.ofCurrentClassPath().entries();
    }
}
