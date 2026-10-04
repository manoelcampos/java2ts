package io.github.manoelcampos.java2ts.fixtures;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Uses wildcards, raw types and arrays of generics. */
public record Wildcards<T extends Comparable<T>>(
    List<?> unknown, List<? extends Number> numbers, List<? super Integer> superIntegers,
    @SuppressWarnings("rawtypes") List rawList, @SuppressWarnings("rawtypes") Map rawMap,
    @SuppressWarnings("rawtypes") Page rawPage, T[] genericArray, Collection<T> values, Object anything, Thread jdkClass)
{
}
