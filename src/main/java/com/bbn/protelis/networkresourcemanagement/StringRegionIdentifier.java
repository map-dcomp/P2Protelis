package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Identifier for a node that just uses a string name.
 */
public class StringRegionIdentifier implements RegionIdentifier, Comparable<StringRegionIdentifier> {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            the name for the region
     */
    public StringRegionIdentifier(@Nonnull final String name) {
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
    public int compareTo(final StringRegionIdentifier other) {
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
        } else if (o instanceof StringRegionIdentifier) {
            return ((StringRegionIdentifier) o).getName().equals(getName());
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
