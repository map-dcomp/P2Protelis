package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Identifier for a node that just uses a string name.
 */
public class StringNodeIdentifier implements NodeIdentifier, Comparable<StringNodeIdentifier> {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            the name for the node
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
        if (this.equals(other)) {
            return 0;
        } else {
            return getName().compareTo(other.getName());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
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
