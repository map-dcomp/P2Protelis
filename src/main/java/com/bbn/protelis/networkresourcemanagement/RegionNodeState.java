package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

/**
 * Detailed resource information for the nodes in a region.
 */
public class RegionNodeState {

    /**
     * 
     * Create an empty regional node state.
     * 
     * @param regionName
     *            the region that this state is for
     */
    public RegionNodeState(@Nonnull final String regionName) {
        this.regionName = regionName;
        this.reports = ImmutableSet.of();
    }

    private final String regionName;

    /**
     * @return the name of the region that this node state is for
     */
    @Nonnull
    public String getRegionName() {
        return this.regionName;
    }

    private ImmutableSet<ResourceReport> reports;

    /**
     * 
     * @return the {@link ResourceReport}s for all nodes in the region.
     */
    @Nonnull
    public ImmutableSet<ResourceReport> getNodeResourceReports() {
        return this.reports;
    }

    /**
     * Modify the set of reports.
     * 
     * @param reports
     *            the new reports
     */
    public void setResourceReports(@Nonnull final ImmutableSet<ResourceReport> reports) {
        this.reports = reports;
    }

}
