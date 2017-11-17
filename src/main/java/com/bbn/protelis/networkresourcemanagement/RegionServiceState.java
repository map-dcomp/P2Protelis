package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

/**
 * Information for the services in a region.
 */
public class RegionServiceState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * Create an empty regional service state.
     * 
     * @param region
     *            the region that this state is for
     */
    public RegionServiceState(@Nonnull final RegionIdentifier region) {
        this.region = region;
        this.reports = ImmutableSet.of();
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this service state is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return this.region;
    }

    private ImmutableSet<ServiceReport> reports;

    /**
     * 
     * @return the {@link ServiceReport}s for all nodes in the region.
     */
    @Nonnull
    public ImmutableSet<ServiceReport> getServiceReports() {
        return this.reports;
    }

    /**
     * Modify the set of reports.
     * 
     * @param reports
     *            the new reports
     */
    public void setServiceReports(@Nonnull final ImmutableSet<ServiceReport> reports) {
        this.reports = reports;
    }

}
