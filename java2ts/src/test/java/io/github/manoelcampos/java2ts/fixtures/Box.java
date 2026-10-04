package io.github.manoelcampos.java2ts.fixtures;

import java.io.Serializable;
import java.util.Map;

/**
 * A generic box with bounded type parameters and maps with different key types.
 * @param <N> a number type
 * @param <P> a page type
 * @param <C> a type with a JDK-only bound, which is ignored
 */
public record Box<N extends Number & Serializable, P extends Page<N> & ModelRecord<N>, C extends Comparable<C>>(
    N number, P page, C comparable,
    Map<Status, Integer> countByStatus, Map<Long, String> namesById, Map<Address, String> objectKeys,
    @Deprecated String oldField)
{
}
