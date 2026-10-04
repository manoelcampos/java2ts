package io.github.manoelcampos.java2ts.sample.dto;

import io.github.manoelcampos.java2ts.sample.model.PersonDTO;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A report showing nested generics and custom type mappings.
 * @param title the report title
 * @param people a page of people DTOs
 * @param coordinates a list of (latitude, longitude) pairs, mapped to a TypeScript tuple array in the plugin configuration
 * @param tags a list of tag groups, which is not affected by the custom mapping of the coordinates
 * @param totals totals by category
 * @param grandTotal the sum of all totals, mapped to string in the plugin configuration
 * @param <N> the type of the numbers in the report
 */
public record Report<N extends Number>(
    @NotNull String title,
    @NotNull PageResponse<PersonDTO> people,
    List<List<Double>> coordinates,
    List<List<String>> tags,
    Map<String, List<N>> totals,
    BigDecimal grandTotal)
{
}
