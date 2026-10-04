package io.github.manoelcampos.java2ts.fixtures;

/**
 * Base class for models (like a Hibernate MappedSuperclass).
 */
public abstract class AbstractModel implements BaseModel, Comparable<AbstractModel> {
    /** The id inherited from {@link BaseModel}. */
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    /** Not a getter, since it has a parameter. */
    public boolean isSameId(final long otherId) {
        return id != null && id == otherId;
    }

    @Override
    public int compareTo(final AbstractModel other) {
        return Long.compare(id, other.id);
    }
}
