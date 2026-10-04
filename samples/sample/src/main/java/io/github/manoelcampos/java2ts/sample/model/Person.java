package io.github.manoelcampos.java2ts.sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A person, which is a JPA entity with getters and setters written by hand.
 */
@Entity
public class Person extends AbstractBaseModel implements Comparable<Person> {
    /** The person's full name. */
    @NotNull @NotBlank @Size(max = 100)
    private String name;

    /** The e-mail, which is optional. */
    @Email
    private String email;

    /** The birth date (converted to string, since it's serialized in ISO-8601 format). */
    @NotNull @Past
    private LocalDate birthDate;

    /** When the person was registered. */
    private LocalDateTime createdAt;

    /** The country where the person lives. */
    @ManyToOne @NotNull
    private Country country;

    /** The partner, if any. */
    @ManyToOne
    private @Nullable Person partner;

    @NotNull @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    /** The person's phones. */
    @ElementCollection @NotNull
    private List<Phone> phones = new ArrayList<>();

    /** Primitive values are always required, since they can't be null. */
    @PositiveOrZero
    private int loginAttempts;

    /** Ignored by Jackson, so it's not included in TypeScript. */
    @JsonIgnore
    private String password;

    /**
     * A property without a field, which is optional since it's an {@link Optional}.
     * @return the person's nickname, if any
     */
    public Optional<String> getNickname() {
        return Optional.ofNullable(name).map(n -> n.split(" ")[0]);
    }

    /**
     * {@return the number of logins by status, just to show how maps with enum keys are converted}
     */
    public Map<Status, Integer> getLoginsByStatus() {
        return new EnumMap<>(Status.class);
    }

    /**
     * Comparable is ignored when generating TypeScript.
     */
    @Override
    public int compareTo(final Person other) {
        return name.compareTo(other.name);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(final Country country) {
        this.country = country;
    }

    public @Nullable Person getPartner() {
        return partner;
    }

    public void setPartner(final @Nullable Person partner) {
        this.partner = partner;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(final List<Phone> phones) {
        this.phones = phones;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(final int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
