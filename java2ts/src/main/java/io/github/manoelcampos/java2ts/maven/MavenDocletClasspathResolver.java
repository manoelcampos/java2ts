package io.github.manoelcampos.java2ts.maven;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.nio.file.Path;
import java.util.List;

/**
 * Resolves the xml-doclet and its dependencies from Maven repositories (downloading them if needed),
 * so that they're only required when the JavaDoc extraction is enabled.
 *
 * @param repositorySystem the Maven repository system
 * @param session the current repository session
 * @param repositories the remote repositories of the project
 * @author Manoel Campos
 */
public record MavenDocletClasspathResolver(
    RepositorySystem repositorySystem, RepositorySystemSession session, List<RemoteRepository> repositories)
    implements DocletClasspathResolver
{
    /** The Maven coordinates of the xml-doclet, without the version. */
    public static final String XML_DOCLET_ARTIFACT = "com.manticore-projects.tools:xml-doclet";

    @Override
    public List<Path> resolve(final String version) {
        final var artifact = new DefaultArtifact(XML_DOCLET_ARTIFACT + ":" + version);
        final var collectRequest = new CollectRequest(new Dependency(artifact, "runtime"), repositories);
        try {
            return repositorySystem.resolveDependencies(session, new DependencyRequest(collectRequest, null))
                                   .getArtifactResults().stream()
                                   .map(ArtifactResult::getArtifact)
                                   .map(resolved -> resolved.getFile().toPath())
                                   .toList();
        } catch (final DependencyResolutionException e) {
            throw new IllegalStateException("Error resolving %s:%s".formatted(XML_DOCLET_ARTIFACT, version), e);
        }
    }
}
