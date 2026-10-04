package io.github.manoelcampos.java2ts.fixtures;

import java.math.BigDecimal;
import java.util.List;

/** Used to test custom type mappings with generic (and nested generic) types. */
public record Measurements(List<BigDecimal> values, List<String> names, List<List<Double>> matrix, StringBuilder builder) {
}
