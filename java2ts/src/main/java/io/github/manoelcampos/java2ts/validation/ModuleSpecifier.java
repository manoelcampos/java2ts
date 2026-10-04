package io.github.manoelcampos.java2ts.validation;

import java.nio.file.Path;
import java.util.List;

/**
 * Creates the module specifier used to import a TypeScript file from another one,
 * such as {@code ./models.generated} or {@code ../model/models.generated}.
 * @author Manoel Campos
 */
final class ModuleSpecifier {
    private static final List<String> EXTENSIONS = List.of(".d.ts", ".ts");
    private static final String CURRENT_DIR = "./";
    private static final String PARENT_DIR = "../";

    private ModuleSpecifier() {/**/}

    /**
     * {@return the relative module specifier to import a file, without its extension}
     * @param importingFile the file that has the import
     * @param importedFile the file to import
     */
    static String of(final Path importingFile, final Path importedFile) {
        final Path from = importingFile.toAbsolutePath().normalize();
        final Path dir = from.getParent() == null ? from : from.getParent();
        final String relative = dir.relativize(importedFile.toAbsolutePath().normalize()).toString().replace('\\', '/');
        final String withoutExtension = removeExtension(relative);
        return withoutExtension.startsWith(PARENT_DIR) ? withoutExtension : CURRENT_DIR + withoutExtension;
    }

    private static String removeExtension(final String path) {
        return EXTENSIONS.stream()
                         .filter(path::endsWith)
                         .findFirst()
                         .map(extension -> path.substring(0, path.length() - extension.length()))
                         .orElse(path);
    }
}
