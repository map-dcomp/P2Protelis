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
     * @param name
     *            the name of the region
     * @return empty plan for a region
     */
    public static RegionPlan getNullPlan(@Nonnull final String name) {
        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> serverCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap.of();
        return new RegionPlan(name, serverCapacity, neighborLinkDemand);
    }

    /**
     * 
     * @param name
     *            the name of the region that the plan is for
     * @param serverCapacity
     *            the planned server capacity
     * @param neighborLinkDemand
     *            the planned link demand
     */
    public RegionPlan(@Nonnull final String name,
            @Nonnull final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> serverCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.regionName = name;
        this.serverCapacity = serverCapacity;
        this.neighborLinkDemand = neighborLinkDemand;
    }

    private final String regionName;

    /**
     * @return name of the region
     */
    @Nonnull
    public String getRegionName() {
        return regionName;
    }

    private final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> serverCapacity;

    /**
     * 
     * @return planned server capacity. Key is service name.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * @return Planned neighbor region link demand. Key is region name.
     */
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

}
