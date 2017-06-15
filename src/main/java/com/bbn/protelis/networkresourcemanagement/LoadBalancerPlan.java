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
     * @param name
     *            the name of the region
     * @return empty plan for a region
     */
    public static LoadBalancerPlan getNullPlan(@Nonnull final String name) {
        final ImmutableMap<String, ImmutableSet<String>> servicePlan = ImmutableMap.of();
        return new LoadBalancerPlan(name, servicePlan);
    }

    /**
     * 
     * @param regionName
     *            name of the region the plan is for
     * @param servicePlan
     *            the service plan
     */
    public LoadBalancerPlan(@Nonnull final String regionName,
            @Nonnull final ImmutableMap<String, ImmutableSet<String>> servicePlan) {
        this.regionName = regionName;
        this.servicePlan = servicePlan;
    }

    private final String regionName;

    /**
     * 
     * @return the region that this plan is for
     */
    @Nonnull
    public String getRegionName() {
        return this.regionName;
    }

    private final ImmutableMap<String, ImmutableSet<String>> servicePlan;

    /**
     * Plan for which services should run on which nodes.
     * 
     * @return Nonn null. Key is node name, value is the list of services.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableSet<String>> getServicePlan() {
        return servicePlan;
    }

}
