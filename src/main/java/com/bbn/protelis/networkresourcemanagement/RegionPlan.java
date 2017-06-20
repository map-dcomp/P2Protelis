package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Plan for the network.
 */
public class RegionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param region
     *            the the region
     * @return empty plan for a region
     */
    public static RegionPlan getNullPlan(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> serverCapacity = ImmutableMap.of();
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap
                .of();
        return new RegionPlan(region, serverCapacity, neighborLinkDemand);
    }

    /**
     * 
     * @param region
     *            the region that the plan is for
     * @param serverCapacity
     *            the planned server capacity
     * @param neighborLinkDemand
     *            the planned link demand
     */
    public RegionPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> serverCapacity,
            @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.region = region;
        this.serverCapacity = serverCapacity;
        this.neighborLinkDemand = neighborLinkDemand;
    }

    private final RegionIdentifier region;

    /**
     * @return the region
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> serverCapacity;

    /**
     * 
     * @return planned server capacity. Key is service name.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * @return Planned neighbor region link demand. Key is region name.
     */
    public ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

}
