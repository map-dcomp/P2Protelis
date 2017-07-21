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
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan = ImmutableMap.of();
        return new RegionPlan(region, plan);
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param plan
     *            see {@link #getPlan()}
     */
    public RegionPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan) {
        this.region = region;
        this.plan = plan;
    }

    private final RegionIdentifier region;

    /**
     * @return the region
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan;

    /**
     * 
     * @return the plan for the region
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> getPlan() {
        return plan;
    }

}
