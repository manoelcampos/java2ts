package io.github.manoelcampos.java2ts.config;

/**
 * The kind of TypeScript file to generate.
 * Both kinds export all declarations, since the generated file is always a module.
 * @author Manoel Campos
 */
public enum OutputFileType {
    /** A declaration file (.d.ts), containing only type declarations. */
    declarationFile(".d.ts"),

    /** An implementation file (.ts). */
    implementationFile(".ts");

    private final String extension;

    OutputFileType(final String extension) {
        this.extension = extension;
    }

    /**
     * {@return the file extension for this type of file, including the leading dot}
     */
    public String extension() {
        return extension;
    }
}
