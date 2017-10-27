package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Plan for balancing services in a region. The load balancer should look at the
 * {@link ResourceReport} and {@link RegionPlan} objects for the current region
 * to come up with a detailed plan of which services should run on which nodes.
 */
public class LoadBalancerPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * This is used as the default plan by Protelis.
     * 
     * @param region
     *            see {@link #getRegion()}
     * @return empty plan for a region
     */
    @Nonnull
    public static LoadBalancerPlan getNullLoadBalancerPlan(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, Integer>> servicePlan = ImmutableMap.of();
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan = ImmutableMap
                .of();
        return new LoadBalancerPlan(region, servicePlan, overflowPlan);
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param servicePlan
     *            see {@link #getServicePlan()}
     * @param overflowPlan
     *            see {@link #getOverflowPlan()}
     */
    public LoadBalancerPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, Integer>> servicePlan,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan) {
        this.regionName = region;
        this.servicePlan = servicePlan;
        this.overflowPlan = overflowPlan;
    }

    private final RegionIdentifier regionName;

    /**
     * @return the region that this plan is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return this.regionName;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, Integer>> servicePlan;

    /**
     * Plan for which services should run on which nodes.
     * 
     * @return the plan. service -> mapping of node and how many instances of
     *         the service
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, Integer>> getServicePlan() {
        return servicePlan;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan;

    /**
     * Plan for handling overflow. This is usually based on
     * {@link RegionPlan#getPlan()} and the current load in the region.
     * 
     * An empty map means all traffic stays local. If the overflow plan is
     * populated and the current region isn't included in the plan, this means
     * that ALL traffic should be sent to other regions.
     * 
     * @return the plan. service -> region to send traffic to -> value, ideally
     *         a value between 0 and 1 that represents a percentage of traffic
     *         to send to the other region
     * @see RegionPlan#getPlan()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> getOverflowPlan() {
        return overflowPlan;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionName, servicePlan, overflowPlan);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof LoadBalancerPlan) {
            final LoadBalancerPlan other = (LoadBalancerPlan) o;
            return Objects.equals(getRegion(), other.getRegion())
                    && Objects.equals(getServicePlan(), other.getServicePlan())
                    && Objects.equals(getOverflowPlan(), other.getOverflowPlan());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + regionName + " servicePlan: " + servicePlan
                + " overflowPlan: " + overflowPlan + " ]";
    }
}
