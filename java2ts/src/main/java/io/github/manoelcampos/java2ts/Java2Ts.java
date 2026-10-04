package io.github.manoelcampos.java2ts;

import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.javadoc.Javadoc;
import io.github.manoelcampos.java2ts.javadoc.XmlJavadoc;
import io.github.manoelcampos.java2ts.parser.ModelParser;
import io.github.manoelcampos.java2ts.parser.ParsedModel;
import io.github.manoelcampos.java2ts.scan.ClassPathContext;
import io.github.manoelcampos.java2ts.scan.ClassSelector;
import io.github.manoelcampos.java2ts.scan.ExclusionFilter;
import io.github.manoelcampos.java2ts.validation.ValidationGenerator;
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
     * the {@link Settings#outputFile()}. When enabled, the validation schemas are written into
     * the {@link io.github.manoelcampos.java2ts.config.ValidationSettings#outputFile(Path)} too.
     * @param context the classpath where classes are looked up
     * @return the path of the generated file
     */
    public Path generateFile(final ClassPathContext context) {
        final ParsedModel model = parse(new ClassSelector(settings.classSelection(), context).select());
        writer().write(model.tsModel(), settings.outputFile());
        if (settings.validation().enabled())
            validationGenerator().generateFile(model);

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
        return writer().toTypeScript(parse(classes).tsModel());
    }

    /**
     * Converts the given classes (and the ones they reference) to validation schemas,
     * ignoring the {@link Settings#classSelection()} (except for exclusions) and
     * whether the {@link Settings#validation()} is enabled.
     * @param classes the classes to convert
     * @return the code of the validation file
     */
    public String generateValidation(final Collection<Class<?>> classes) {
        return validationGenerator().generate(parse(classes));
    }

    private ParsedModel parse(final Collection<Class<?>> classes) {
        final Javadoc javadoc = XmlJavadoc.load(settings.javadocXmlFiles());
        return new ModelParser(settings, javadoc, exclusion()).parse(classes);
    }

    private ValidationGenerator validationGenerator() {
        return new ValidationGenerator(settings, exclusion());
    }

    private ExclusionFilter exclusion() {
        return new ExclusionFilter(settings.classSelection());
    }

    private TypeScriptWriter writer() {
        return new TypeScriptWriter(new FileHeader(!settings.noFileDate()));
    }
}
