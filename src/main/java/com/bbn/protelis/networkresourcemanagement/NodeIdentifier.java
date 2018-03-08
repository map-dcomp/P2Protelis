package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;

/**
 * Opaque identifier for a node. Implementations should be immutable and must be
 * {@link Comparable}. Node names shall be compared in a case-insensitive
 * manner.
 */
public interface NodeIdentifier extends DeviceUID {

    /**
     * @return the name of the node.
     */
    @Nonnull
    String getName();
}
