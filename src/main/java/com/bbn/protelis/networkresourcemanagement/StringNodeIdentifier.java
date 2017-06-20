package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

/**
 * Identifier for a node that just uses a string name.
 */
public class StringNodeIdentifier implements NodeIdentifier, Comparable<StringNodeIdentifier> {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            the name for the service
     */
    public StringNodeIdentifier(@Nonnull final String name) {
        this.name = name;
    }

    private final String name;

    /**
     * 
     * @return the underlying string.
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(final StringNodeIdentifier other) {
        if (Objects.equal(this, other)) {
            return 0;
        } else {
            return getName().compareTo(other.getName());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (Objects.equal(this, o)) {
            return true;
        } else if (o instanceof StringNodeIdentifier) {
            return ((StringNodeIdentifier) o).getName().equals(getName());
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
