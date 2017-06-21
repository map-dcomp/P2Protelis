package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * Identifier for a region. Implementations should be immutable and must be
 * {@link Comparable}.
 */
public interface RegionIdentifier extends Serializable {

    /**
     * @return the name of the region
     */
    @Nonnull
    String getName();
}
