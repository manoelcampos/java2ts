package io.github.manoelcampos.java2ts.sample.dto;

import io.github.manoelcampos.java2ts.sample.model.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * A hand-written Data Transfer Object for {@link Country}.
 * Record components become read-only TypeScript properties.
 * @param id the country id, which is null for countries not saved yet
 * @param name the country name
 * @param code the ISO 3166-1 alpha-2 code, such as BR
 */
public record CountryDTO(
    @Nullable Long id,
    @NotNull @NotBlank String name,
    @NotNull @Size(min = 2, max = 2) String code)
{
    /**
     * Creates a DTO from a country.
     * @param country the country to get the values from
     */
    public CountryDTO(final Country country) {
        this(country.getId(), country.getName(), country.getCode());
    }
}
