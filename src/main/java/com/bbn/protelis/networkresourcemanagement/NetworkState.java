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
        this.shortRegionSummary = ResourceSummary.getNullSummary(region, ResourceReport.EstimationWindow.SHORT);
        this.longRegionSummary = ResourceSummary.getNullSummary(region, ResourceReport.EstimationWindow.LONG);
        this.regionPlan = RegionPlan.getNullRegionPlan(region);
        this.loadBalancerPlan = LoadBalancerPlan.getNullLoadBalancerPlan(region);
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this network state is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private ResourceSummary shortRegionSummary;
    private ResourceSummary longRegionSummary;

    /**
     * @param estimationWindow
     *            the estimation window for the demand
     * @return the summary for the region.
     */
    @Nonnull
    public ResourceSummary getRegionSummary(@Nonnull final ResourceReport.EstimationWindow estimationWindow) {
        switch (estimationWindow) {
        case LONG:
            return longRegionSummary;
        case SHORT:
            return shortRegionSummary;
        default:
            throw new IllegalArgumentException("Unknown estimation window: " + estimationWindow);
        }
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

        switch (summary.getDemandEstimationWindow()) {
        case LONG:
            longRegionSummary = summary;
            break;
        case SHORT:
            shortRegionSummary = summary;
            break;
        default:
            throw new IllegalArgumentException("Unknown estimation window: " + summary.getDemandEstimationWindow());
        }
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
            throw new IllegalArgumentException("Region plan (" + plan.getRegion()
                    + ") must be for the same region as the network state object (" + getRegion() + ")");
        }

        this.regionPlan = plan;
    }

    private LoadBalancerPlan loadBalancerPlan;

    /**
     * @return the current plan for the region
     */
    @Nonnull
    public LoadBalancerPlan getLoadBalancerPlan() {
        return loadBalancerPlan;
    }

    /**
     * 
     * @param plan
     *            the new plan for the region
     * @throws IllegalArgumentException
     *             if the plan is for a different region than the node
     */
    public void setLoadBalancerPlan(@Nonnull final LoadBalancerPlan plan) {
        if (!plan.getRegion().equals(this.region)) {
            throw new IllegalArgumentException("Load balancer plan (" + plan.getRegion()
                    + ") must be for the same region as the network state object (" + getRegion() + ")");
        }

        this.loadBalancerPlan = plan;
    }

}
