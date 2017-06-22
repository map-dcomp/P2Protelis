package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Contains the network information for a region.
 */
public class NetworkState {

    /**
     * Create an object with no known network state.
     *
     * @param region
     *            the region that this state is for
     */
    public NetworkState(final RegionIdentifier region) {
        this.region = region;
        this.regionSummary = ResourceSummary.getNullSummary(region);
        this.regionPlan = RegionPlan.getNullPlan(region);
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this network state is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private ResourceSummary regionSummary;

    /**
     * 
     * @return the summary for the region.
     */
    @Nonnull
    public ResourceSummary getRegionSummary() {
        return this.regionSummary;
    }

    /**
     * Specify a new summary for the region.
     * 
     * @param summary
     *            the new summary
     * 
     * @throws IllegalArgumentException
     *             if the summary is for a different region than the node
     */
    public void setRegionSummary(@Nonnull final ResourceSummary summary) {
        if (!summary.getRegion().equals(this.region)) {
            throw new IllegalArgumentException(
                    "Region summary must be for the same region as the network state object");
        }

        regionSummary = summary;
    }

    private RegionPlan regionPlan;

    /**
     * @return the current plan for the region
     */
    @Nonnull
    public RegionPlan getRegionPlan() {
        return regionPlan;
    }

    /**
     * 
     * @param plan
     *            the new plan for the region
     * @throws IllegalArgumentException
     *             if the plan is for a different region than the node
     */
    public void setRegionPlan(@Nonnull final RegionPlan plan) {
        if (!plan.getRegion().equals(this.region)) {
            throw new IllegalArgumentException(
                    "Region summary must be for the same region as the network state object");
        }

        this.regionPlan = plan;
    }

}
