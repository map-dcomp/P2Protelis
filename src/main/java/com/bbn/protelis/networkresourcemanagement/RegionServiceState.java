/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
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
        this.hashCode = Objects.hashCode(this.region, this.reports);
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

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        } else if (o == this) {
            return true;
        } else if (getClass().equals(o.getClass())) {
            final RegionServiceState other = (RegionServiceState) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return getRegion().equals(other.getRegion()) && getServiceReports().equals(other.getServiceReports());
            }
        } else {
            return false;
        }
    }
}
