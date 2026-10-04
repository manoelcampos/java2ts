package io.github.manoelcampos.java2ts;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.javadoc.XmlJavadoc;
import io.github.manoelcampos.java2ts.parser.ModelParser;
import io.github.manoelcampos.java2ts.scan.ClassPathContext;
import io.github.manoelcampos.java2ts.scan.ClassSelector;
import io.github.manoelcampos.java2ts.scan.ExclusionFilter;
import io.github.manoelcampos.java2ts.ts.TsModel;
import io.github.manoelcampos.java2ts.writer.FileHeader;
import io.github.manoelcampos.java2ts.writer.TypeScriptWriter;

import java.nio.file.Path;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Entry point to convert Java classes into a TypeScript file (Facade pattern).
 * @author Manoel Campos
 */
public final class Java2Ts {
    private final Settings settings;

    /**
     * Creates a converter.
     * @param settings the conversion settings
     */
    public Java2Ts(final Settings settings) {
        this.settings = requireNonNull(settings);
    }

    /**
     * Converts the classes selected by the {@link Settings#classSelection()} and writes them into
     * the {@link Settings#outputFile()}.
     * @param context the classpath where classes are looked up
     * @return the path of the generated file
     */
    public Path generateFile(final ClassPathContext context) {
        final TsModel model = parse(new ClassSelector(settings.classSelection(), context).select());
        writer().write(model, settings.outputFile());
        return settings.outputFile();
    }

    /**
     * Converts the classes selected by the {@link Settings#classSelection()} to TypeScript code.
     * @param context the classpath where classes are looked up
     * @return the TypeScript code
     */
    public String generate(final ClassPathContext context) {
        return generate(new ClassSelector(settings.classSelection(), context).select());
    }

    /**
     * Converts the given classes (and the ones they reference) to TypeScript code,
     * ignoring the {@link Settings#classSelection()} (except for exclusions).
     * @param classes the classes to convert
     * @return the TypeScript code
     */
    public String generate(final Collection<Class<?>> classes) {
        return writer().toTypeScript(parse(classes));
    }

    private TsModel parse(final Collection<Class<?>> classes) {
        final Javadoc javadoc = XmlJavadoc.load(settings.javadocXmlFiles());
        final var exclusion = new ExclusionFilter(settings.classSelection());
        return new ModelParser(settings, javadoc, exclusion).parse(classes);
    }

    private TypeScriptWriter writer() {
        return new TypeScriptWriter(new FileHeader(!settings.noFileDate()));
    }
}
