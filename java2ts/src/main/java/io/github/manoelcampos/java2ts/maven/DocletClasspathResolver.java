package io.github.manoelcampos.java2ts.maven;

import java.nio.file.Path;
import java.util.List;

/**
 * Resolves the jar files of the xml-doclet and its dependencies.
 * @author Manoel Campos
 */
@FunctionalInterface
public interface DocletClasspathResolver {
    /**
     * {@return the jar files of the xml-doclet and its dependencies}
     * @param version the xml-doclet version
     */
    List<Path> resolve(String version);
}
