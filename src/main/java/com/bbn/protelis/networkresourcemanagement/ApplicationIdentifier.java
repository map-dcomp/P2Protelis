package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import org.kie.api.management.GAV;

import com.google.common.collect.ComparisonChain;

/**
 * Identifier for an application, using the {group, artifact, version} triplet, as personifed by the GAV value class
 */
public class ApplicationIdentifier implements ServiceIdentifier<GAV>, Comparable<ApplicationIdentifier> {

    private static final long serialVersionUID = -2036436355195990247L;

    /**
     * 
     * @param GAV
     * the applicaiton coordinates
     */
    public ApplicationIdentifier(@Nonnull final GAV coordinates) {
        this.coordinates = coordinates;
    }

    private final GAV coordinates;
    
    @Override
    public int compareTo(final ApplicationIdentifier other) {
        return ComparisonChain.start()
                .compare(other.getIdentifier().getGroupId(), this.getIdentifier().getGroupId())
                .compare(other.getIdentifier().getArtifactId(),  this.getIdentifier().getArtifactId())
                .compare(other.getIdentifier().getVersion(),  this.getIdentifier().getVersion())
                .result();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ApplicationIdentifier) {
            return ((ApplicationIdentifier) o).getIdentifier().equals(getIdentifier());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return coordinates.hashCode();
    }

    @Override
    public String toString() {
        return coordinates.toExternalForm();
    }

    @Override
    public GAV getIdentifier() {
        return coordinates;
    }

}