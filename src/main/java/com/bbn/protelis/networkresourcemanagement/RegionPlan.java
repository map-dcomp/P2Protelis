package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Plan for the network. This specifies a recommendation on how traffic should
 * be pushed from the {@link #getRegion()} to it's neighboring regions.
 */
public class RegionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * This is used as the default object by Protelis.
     * 
     * @param region
     *            see {@link #getRegion()}
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
     * @return the region that this plan is for.
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan;

    /**
     * This specifies how much traffic for each service should be pushed from
     * this region to neighboring regions. The value ideally should be a number
     * between 0 and 1 and represent a percentage.
     * 
     * @return the plan for the region. service -> neighbor region -> value.
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + region + " plan: " + plan + " ]";
    }

}
