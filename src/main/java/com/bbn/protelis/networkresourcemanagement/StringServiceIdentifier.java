package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

/**
 * Identifier for a service that just uses a string name.
 */
public class StringServiceIdentifier implements ServiceIdentifier {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            the name for the service
     */
    public StringServiceIdentifier(@Nonnull final String name) {
        this.name = name;
    }

    private final String name;

    /**
     * @return the underlying string
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Compare names if a {@link StringServiceIdentifier}, otherwise compare
     * toString of other to name of this.
     */
    @Override
    public int compareTo(final ServiceIdentifier other) {
        if (Objects.equal(this, other)) {
            return 0;
        } else if (other instanceof StringServiceIdentifier) {
            return ((StringServiceIdentifier) other).getName().compareTo(getName());
        } else {
            return other.toString().compareTo(getName());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (Objects.equal(this, o)) {
            return true;
        } else if (o instanceof StringServiceIdentifier) {
            return ((StringServiceIdentifier) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

}
