package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Contains the network information known to an individual {@link Node}.
 */
public class NetworkState {

    /**
     * Create an object with no known network state.
     *
     * @param node
     *            the node that this state is attached to
     */
    public NetworkState(@Nonnull final Node node) {
        this.node = node;
        this.regionSummary = RegionSummary.getNullSummary(node.getName());
        this.regionPlan = RegionPlan.getNullPlan(node.getName());
    }

    private final Node node;

    /**
     * @return the node that this network state is for
     */
    @Nonnull
    public Node getNode() {
        return node;
    }

    private RegionSummary regionSummary;

    /**
     * 
     * @return the summary for the region.
     */
    @Nonnull
    public RegionSummary getRegionSummary() {
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
    public void setRegionSummary(@Nonnull final RegionSummary summary) {
        if (!summary.getRegionName().equals(getNode().getRegionName())) {
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
        if (!plan.getRegionName().equals(getNode().getRegionName())) {
            throw new IllegalArgumentException(
                    "Region summary must be for the same region as the network state object");
        }

        this.regionPlan = plan;
    }

}
