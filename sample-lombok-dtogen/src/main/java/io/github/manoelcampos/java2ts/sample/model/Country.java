package io.github.manoelcampos.java2ts.sample.model;

import io.github.manoelcampos.dtogen.DTO;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * A country where people live.
 */
@Entity @Getter @Setter @DTO
public class Country extends AbstractBaseModel {
    /** The country name. */
    @NotNull @NotBlank
    private String name;

    /** The ISO 3166-1 alpha-2 code, such as BR. */
    @NotNull @Size(min = 2, max = 2)
    private String code;
}
