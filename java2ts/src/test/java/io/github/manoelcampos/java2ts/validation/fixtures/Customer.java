package io.github.manoelcampos.java2ts.validation.fixtures;

import io.github.manoelcampos.java2ts.fixtures.Nullable;
import io.github.manoelcampos.java2ts.fixtures.Required;
import io.github.manoelcampos.java2ts.fixtures.Status;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A record with all kinds of supported constraints.
 */
public record Customer(
    @Required UUID id,
    @Required @NotBlank(message = "The name is required") @Size(max = 50) String name,
    @Email String email,
    @Min(18) @Max(120) int age,
    @DecimalMin(value = "0", inclusive = false) Double credit,
    @Pattern(regexp = "[a-z]+/\\d+", flags = Pattern.Flag.CASE_INSENSITIVE) String code,
    @Size(min = 1, message = "At least {min} tag") List<@NotBlank String> tags,
    @Past LocalDate birthDate,
    @FutureOrPresent Instant expiresAt,
    LocalDateTime createdAt,
    @AssertTrue boolean active,
    @Digits(integer = 5, fraction = 2) BigDecimal balance,
    @Positive Long points,
    @Required Status status,
    Map<Status, Integer> counters,
    Map<Integer, String> names,
    @Nullable Customer partner,
    @URL String site,
    char initial,
    byte[] photo,
    Optional<String> nickname,
    @Length(min = 2, max = 2) String state,
    @Range(min = 1, max = 5) int rating,
    @NotNull @Size(min = 3, max = 3) String currency)
{
}
