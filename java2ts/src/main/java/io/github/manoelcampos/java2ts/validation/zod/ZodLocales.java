package io.github.manoelcampos.java2ts.validation.zod;

import java.util.Locale;
import java.util.Set;

/**
 * Converts language tags (such as {@code pt-BR}) into the names of Zod's built-in locales (such as {@code ptBR}).
 * Most Zod locales are named after the language (such as {@code pt} and {@code en}),
 * but a few ones also include the region.
 * A language that Zod doesn't support is written anyway, so that the TypeScript compiler reports it.
 * @author Manoel Campos
 */
final class ZodLocales {
    /** Zod locales that are specific to a region. */
    private static final Set<String> REGIONAL_LOCALES = Set.of("frCA", "ptBR", "zhCN", "zhTW");

    private ZodLocales() {/**/}

    /**
     * {@return the name of the Zod locale for a language tag}
     * @param languageTag a BCP 47 language tag, such as {@code pt-BR} or {@code en}
     * @throws IllegalArgumentException if the language tag is invalid
     */
    static String of(final String languageTag) {
        final Locale locale = Locale.forLanguageTag(languageTag.strip().replace('_', '-'));
        final String language = locale.getLanguage();
        if (language.isEmpty())
            throw new IllegalArgumentException("Invalid validation locale '%s'. Use a language tag such as pt-BR".formatted(languageTag));

        final String regional = language + locale.getCountry();
        return REGIONAL_LOCALES.contains(regional) ? regional : language;
    }
}
