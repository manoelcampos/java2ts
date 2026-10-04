package io.github.manoelcampos.java2ts;

import io.github.manoelcampos.java2ts.config.OptionalPropertiesDeclaration;
import io.github.manoelcampos.java2ts.config.SettingsBuilder;
import io.github.manoelcampos.java2ts.config.Settings;
import io.github.manoelcampos.java2ts.fixtures.Nullable;
import io.github.manoelcampos.java2ts.fixtures.Required;

import java.util.Set;

/**
 * Settings used by tests, similar to the ones used in real projects.
 */
public final class TestSettings {
    public static final String FIXTURES_PACKAGE = "io.github.manoelcampos.java2ts.fixtures";

    private TestSettings() {/**/}

    /**
     * {@return a settings builder with nullable/required annotations and the
     * {@link OptionalPropertiesDeclaration#questionMarkAndNullableType}}
     */
    public static SettingsBuilder builder() {
        return Settings.builder()
                       .nullableAnnotations(Set.of(Nullable.class.getName()))
                       .requiredAnnotations(Set.of(Required.class.getName()))
                       .optionalPropertiesDeclaration(OptionalPropertiesDeclaration.questionMarkAndNullableType)
                       .noFileDate(true);
    }
}
