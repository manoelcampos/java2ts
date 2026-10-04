package io.github.manoelcampos.java2ts.fixtures;

/** Like the DTORecord interface from DTOGen: it has only methods that aren't getters. */
public interface ModelRecord<T> {
    T toModel();
}
