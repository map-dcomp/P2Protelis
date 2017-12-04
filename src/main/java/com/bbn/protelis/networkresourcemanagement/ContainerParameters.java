package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Parameters to start a container. See
 * {@link ResourceManager#startService(ServiceIdentifier, ContainerParameters)}
 * for usage.
 * 
 * @author jschewe
 *
 */
public final class ContainerParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param computeCapacity
     *            see {@link #getComputeCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     */
    public ContainerParameters(final @Nonnull ImmutableMap<NodeAttribute<?>, Double> computeCapacity,
            final @Nonnull ImmutableMap<LinkAttribute<?>, Double> networkCapacity) {
        this.computeCapacity = computeCapacity;
        this.networkCapacity = networkCapacity;
    }

    private final ImmutableMap<NodeAttribute<?>, Double> computeCapacity;

    /**
     * 
     * @return the compute capacity for the container
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getComputeCapacity() {
        return computeCapacity;
    }

    private final ImmutableMap<LinkAttribute<?>, Double> networkCapacity;

    /**
     * 
     * @return the network capacity for the container. The values are applied to
     *         all directly connected nodes.
     */
    @Nonnull
    public ImmutableMap<LinkAttribute<?>, Double> getNetworkCapacity() {
        return networkCapacity;
    }
}
