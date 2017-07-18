package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Plan for balancing services.
 */
public class LoadBalancerPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param region
     *            the region
     * @return empty plan for a region
     */
    @Nonnull
    public static LoadBalancerPlan getNullPlan(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableSet<NodeIdentifier>> servicePlan = ImmutableMap.of();
        return new LoadBalancerPlan(region, servicePlan);
    }

    /**
     * 
     * @param region
     *            the region the plan is for
     * @param servicePlan
     *            the service plan
     */
    public LoadBalancerPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableSet<NodeIdentifier>> servicePlan) {
        this.regionName = region;
        this.servicePlan = servicePlan;
    }

    private final RegionIdentifier regionName;

    /**
     * 
     * @return the region that this plan is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return this.regionName;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableSet<NodeIdentifier>> servicePlan;

    /**
     * Plan for which services should run on which nodes.
     * 
     * @return the plan
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableSet<NodeIdentifier>> getServicePlan() {
        return servicePlan;
    }

}
