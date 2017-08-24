package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.Objects;

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
    public static LoadBalancerPlan getNullLoadBalancerPlan(@Nonnull final RegionIdentifier region) {
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

    @Override
    public int hashCode() {
        return Objects.hash(regionName, servicePlan);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof LoadBalancerPlan) {
            final LoadBalancerPlan other = (LoadBalancerPlan) o;
            return Objects.equals(getRegion(), other.getRegion())
                    && Objects.equals(getServicePlan(), other.getServicePlan());
        } else {
            return false;
        }
    }

    /**
     * Compare 2 plans ignoring the timestamp.
     * 
     * @param other
     *            the plan to compare against this plan
     * @return if the 2 plans are equivalent
     */
    public boolean equivalentTo(final LoadBalancerPlan other) {
        return Objects.equals(getRegion(), other.getRegion())
                && Objects.equals(getServicePlan(), other.getServicePlan());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + regionName + " servicePlan: " + servicePlan
                + " ]";
    }
}
