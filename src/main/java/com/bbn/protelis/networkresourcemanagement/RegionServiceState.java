package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
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
     *            see {@link #getRegion()}
     * @param reports
     *            See {@link #getServiceReports(){
     */
    public RegionServiceState(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableSet<ServiceReport> reports) {
        this.region = region;
        this.reports = reports;
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

    @Override
    public String toString() {
        return "{" + " region: " + getRegion() + " reports: " + getServiceReports() + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getRegion(), getServiceReports());
    }

    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        } else if (o == this) {
            return true;
        } else if (getClass().equals(o.getClass())) {
            final RegionServiceState other = (RegionServiceState) o;

            return getRegion().equals(other.getRegion()) && getServiceReports().equals(other.getServiceReports());
        } else {
            return false;
        }
    }
}
