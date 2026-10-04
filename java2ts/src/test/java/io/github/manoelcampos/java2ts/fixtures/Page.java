package io.github.manoelcampos.java2ts.fixtures;

import java.util.List;

/** A generic page of items. */
public class Page<T> {
    private List<T> items;
    private long total;

    public List<T> getItems() { return items; }
    public long getTotal() { return total; }
}
