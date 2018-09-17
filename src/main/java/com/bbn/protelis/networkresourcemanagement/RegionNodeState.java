package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

/**
 * Detailed resource information for the nodes in a region.
 */
public class RegionNodeState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * Create an empty regional node state.
     * 
     * @param region
     *            the region that this state is for
     */
    public RegionNodeState(@Nonnull final RegionIdentifier region) {
        this.region = region;
        this.reports = ImmutableSet.of();
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this node state is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return this.region;
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
