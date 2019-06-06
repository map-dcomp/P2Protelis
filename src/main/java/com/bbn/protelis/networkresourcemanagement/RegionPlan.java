/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
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
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public RegionPlan(@JsonProperty("region") @Nonnull final RegionIdentifier region,
            @JsonProperty("plan") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan) {
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
     * An empty map means all traffic stays local. If the plan is populated and
     * the current region isn't included in the plan, this means that ALL
     * traffic should be sent to other regions.
     * 
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
