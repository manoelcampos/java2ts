package io.github.manoelcampos.java2ts.sample.dto;

import io.github.manoelcampos.java2ts.sample.model.Person;
import io.github.manoelcampos.java2ts.sample.model.Phone;
import io.github.manoelcampos.java2ts.sample.model.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/**
 * A hand-written Data Transfer Object for {@link Person}, which references
 * associated entities by their ids. Record components become read-only TypeScript properties.
 * @param id the person id, which is null for people not saved yet
 * @param name the person's full name
 * @param email the e-mail, which is optional
 * @param birthDate the birth date
 * @param countryId the id of the country where the person lives
 * @param partnerId the id of the partner, if any
 * @param status the person status
 * @param phones the person's phones
 * @param loginAttempts the number of failed login attempts
 */
public record PersonDTO(
    @Nullable Long id,
    @NotNull @NotBlank @Size(max = 100) String name,
    @Email String email,
    @NotNull @Past LocalDate birthDate,
    long countryId,
    @Nullable Long partnerId,
    @NotNull Status status,
    @NotNull List<Phone> phones,
    @PositiveOrZero int loginAttempts)
{
    /**
     * Creates a DTO from a person.
     * @param person the person to get the values from
     */
    public PersonDTO(final Person person) {
        this(
            person.getId(), person.getName(), person.getEmail(), person.getBirthDate(),
            person.getCountry().getId(), partnerId(person), person.getStatus(),
            List.copyOf(person.getPhones()), person.getLoginAttempts());
    }

    private static @Nullable Long partnerId(final Person person) {
        final Person partner = person.getPartner();
        return partner == null ? null : partner.getId();
    }
}
