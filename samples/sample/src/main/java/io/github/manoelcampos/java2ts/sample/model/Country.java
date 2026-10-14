package io.github.manoelcampos.java2ts.sample.model;

import io.github.manoelcampos.java2ts.sample.validation.CountryCode;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A country where people live.
 */
@Entity
public class Country extends AbstractBaseModel {
    /** The country name. */
    @NotNull @NotBlank
    private String name;

    /** The ISO 3166-1 alpha-2 code, such as BR. */
    @CountryCode
    private String code;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }
}
