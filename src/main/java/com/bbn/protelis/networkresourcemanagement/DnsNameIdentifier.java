package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;

/**
 * Identifier that represents a DNS name. The name is compared in a
 * case-insensitive manner.
 */
public class DnsNameIdentifier implements ContainerIdentifier, DeviceUID, Comparable<DnsNameIdentifier> {
    private static final long serialVersionUID = 1L;

    private final String name;

    /**
     * 
     * @param name
     *            the name for the node
     */
    public DnsNameIdentifier(@Nonnull final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object alt) {
        if (null == alt) {
            return false;
        } else if (this == alt) {
            return true;
        } else if (getClass().equals(alt.getClass())) {
            final DnsNameIdentifier other = (DnsNameIdentifier) alt;
            return getName().equalsIgnoreCase(other.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(final DnsNameIdentifier other) {
        return getName().toLowerCase().compareTo(other.getName().toLowerCase());
    }
}
