package io.github.manoelcampos.java2ts.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * Creates a class loader for the classpath of a Maven project, isolated from the plugin classes.
 * @author Manoel Campos
 */
final class ProjectClassLoader {
    private ProjectClassLoader() {/**/}

    /**
     * {@return a new class loader for the given classpath entries}
     * @param classpath the classpath entries (directories and jar files)
     */
    static URLClassLoader create(final List<Path> classpath) {
        final URL[] urls = classpath.stream().map(ProjectClassLoader::toUrl).toArray(URL[]::new);
        return new URLClassLoader(urls, ClassLoader.getPlatformClassLoader());
    }

    private static URL toUrl(final Path path) {
        try {
            return path.toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Invalid classpath entry " + path, e);
        }
    }
}
