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
     * The timestamp for the null plan is {@link Long#MIN_VALUE} to ensure it's
     * older than another other plan.
     * 
     * @param region
     *            the the region
     * @return empty plan for a region
     */
    public static RegionPlan getNullRegionPlan(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan = ImmutableMap.of();
        return new RegionPlan(Long.MIN_VALUE, region, plan);
    }

    /**
     * 
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param region
     *            see {@link #getRegion()}
     * @param plan
     *            see {@link #getPlan()}
     */
    public RegionPlan(final long timestamp,
            @Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan) {
        this.region = region;
        this.plan = plan;
        this.timestamp = timestamp;
    }

    private final long timestamp;

    /**
     * The units of the timestamp are determinted by the clock used for the
     * network. Possible examples may be milliseconds since the epoch or
     * milliseconds since the start of the application. It is not expected that
     * this time be converted to a date time for display to the user. This value
     * is used to differentiate determine which is the newest plan.
     * 
     * @return when the plan was generated
     */
    public long getTimestamp() {
        return timestamp;
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
        return Objects.hash(timestamp, region, plan);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof RegionPlan) {
            final RegionPlan other = (RegionPlan) o;
            return getTimestamp() == other.getTimestamp() && Objects.equals(getRegion(), other.getRegion())
                    && Objects.equals(getPlan(), other.getPlan());
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
        return this.getClass().getSimpleName() + " [" + " timestamp: " + timestamp + " region: " + region + " plan: "
                + plan + " ]";
    }

}
