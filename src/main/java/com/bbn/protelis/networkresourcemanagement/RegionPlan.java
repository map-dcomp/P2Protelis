package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.Objects;

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
    public static RegionPlan getNullRegionPlan(@Nonnull final RegionIdentifier region) {
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

    @Override
    public int hashCode() {
        return Objects.hash(region, plan);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof RegionPlan) {
            final RegionPlan other = (RegionPlan) o;
            return Objects.equals(getRegion(), other.getRegion()) && Objects.equals(getPlan(), other.getPlan());
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
    public boolean equivalentTo(final RegionPlan other) {
        return Objects.equals(getRegion(), other.getRegion()) && Objects.equals(getPlan(), other.getPlan());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + region + " plan: " + plan + " ]";
    }

}
