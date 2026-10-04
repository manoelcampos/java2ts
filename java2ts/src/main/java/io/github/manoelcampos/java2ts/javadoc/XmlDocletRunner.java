package io.github.manoelcampos.java2ts.javadoc;

import jdk.javadoc.doclet.Doclet;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs the <a href="https://github.com/manticore-projects/xml-doclet">xml-doclet</a> in the current JVM
 * to extract the JavaDocs of Java source files into a XML file.
 * The doclet is loaded in an isolated class loader, so that its dependencies don't conflict with the application ones.
 * @author Manoel Campos
 */
public final class XmlDocletRunner {
    /** Fully qualified name of the xml-doclet class. */
    public static final String DOCLET_CLASS = "com.manticore.tools.xmldoclet.XmlDoclet";
    private final List<Path> docletClasspath;

    /**
     * Creates a doclet runner.
     * @param docletClasspath the jar files of the xml-doclet and its dependencies
     */
    public XmlDocletRunner(final List<Path> docletClasspath) {
        this.docletClasspath = List.copyOf(docletClasspath);
    }

    /**
     * Extracts the JavaDocs of all Java source files inside some directories into a XML file.
     * @param sourceRoots the directories containing Java source files
     * @param classpath the classpath required to compile the source files
     * @param outputXml the path of the XML file to generate
     * @return an Optional with the path of the generated XML file, or an empty Optional if no source file was found
     * @throws IllegalStateException if the JavaDoc tool fails
     */
    public Optional<Path> run(final Collection<Path> sourceRoots, final Collection<Path> classpath, final Path outputXml) {
        final List<Path> sources = JavaSourceFinder.find(sourceRoots);
        if (sources.isEmpty())
            return Optional.empty();

        try (var docletLoader = new URLClassLoader(toUrls(docletClasspath), Doclet.class.getClassLoader())) {
            final Class<? extends Doclet> docletClass = docletLoader.loadClass(DOCLET_CLASS).asSubclass(Doclet.class);
            Files.createDirectories(parentDir(outputXml));
            runDoclet(docletClass, sources, options(classpath, outputXml));
            return Optional.of(outputXml);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("xml-doclet not found in the classpath " + docletClasspath, e);
        }
    }

    private static void runDoclet(final Class<? extends Doclet> docletClass, final List<Path> sources, final List<String> options) throws IOException {
        final DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
        final var output = new StringWriter();
        final Thread thread = Thread.currentThread();
        final ClassLoader previousLoader = thread.getContextClassLoader();
        try (var fileManager = tool.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            final var units = fileManager.getJavaFileObjectsFromPaths(sources);
            final boolean success = tool.getTask(output, fileManager, null, docletClass, options, units).call();
            if (!success)
                throw new IllegalStateException("Error extracting JavaDocs with the xml-doclet:%n%s".formatted(output));
        } finally {
            // The xml-doclet changes the context class loader
            thread.setContextClassLoader(previousLoader);
        }
    }

    private static List<String> options(final Collection<Path> classpath, final Path outputXml) {
        final String classpathStr = classpath.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
        return List.of(
            "-private", "-quiet", "-encoding", StandardCharsets.UTF_8.name(),
            "-classpath", classpathStr,
            "-d", parentDir(outputXml).toString(),
            "-filename", String.valueOf(outputXml.getFileName()));
    }

    private static Path parentDir(final Path file) {
        final Path parent = file.toAbsolutePath().getParent();
        if (parent == null)
            throw new IllegalArgumentException("Invalid output file " + file);

        return parent;
    }

    private static URL[] toUrls(final List<Path> paths) {
        return paths.stream().map(XmlDocletRunner::toUrl).toArray(URL[]::new);
    }

    private static URL toUrl(final Path path) {
        try {
            return path.toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Invalid classpath entry " + path, e);
        }
    }
}
