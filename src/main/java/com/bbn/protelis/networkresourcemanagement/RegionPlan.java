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
import java.util.Objects;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.ComparisonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Plan for the network. This specifies a recommendation on how traffic should
 * be pushed from the {@link #getRegion()} to it's neighboring regions.
 */
public class RegionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp for null reports.
     */
    public static final long NULL_TIMESTAMP = -1;

    /**
     * This is used as the default object by Protelis.
     * 
     * @param region
     *            see {@link #getRegion()}
     * @return empty plan for a region
     */
    public static RegionPlan getNullRegionPlan(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan = ImmutableMap.of();
        return new RegionPlan(region, NULL_TIMESTAMP, plan);
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param plan
     *            see {@link #getPlan()}
     */
    public RegionPlan(@JsonProperty("region") @Nonnull final RegionIdentifier region,
            @JsonProperty("timestamp") @Nonnull final long timestamp,
            @JsonProperty("plan") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan) {
        this.region = region;
        this.timestamp = timestamp;
        this.plan = plan;
        this.hashCode = Objects.hash(this.region, this.plan);
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param plan
     *            see {@link #getPlan()}
     */
    public RegionPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan) {
        this.region = region;
        this.plan = plan;
        // don't include anything with a fuzzy match in equals
        this.hashCode = this.region.hashCode();
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this plan is for.
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private long timestamp;

    /**
     * @param timestamp
     *            see {@link #getTimestamp()}
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * The units of the timestamp are determined by the clock used for the
     * network. Possible examples may be milliseconds since the epoch or
     * milliseconds since the start of the application. It is not expected that
     * this time be converted to a date time for display to the user. This value
     * is used to differentiate 2 plans created at different times.
     * 
     * @return when the plan was created
     */
    public long getTimestamp() {
        return timestamp;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> plan;

    /**
     * This specifies how much traffic for each service should be pushed from
     * this region to neighboring regions. The value ideally should be a number
     * between 0 and 1 and represent a percentage.
     * 
     * An empty map means all traffic stays local, if possible. If the plan is
     * populated and the current region isn't included in the plan, this means
     * that ALL traffic should be sent to other regions. See
     * {@link LoadBalancerPlan#getOverflowPlan()} for more information.
     * 
     * 
     * @return the plan for the region. service -> neighbor region -> value.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> getPlan() {
        return plan;
    }

    private final int hashCode;

    /**
     * Creates a hash for this plan without considering the timestamp.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Checks for equality between the content of this plan and another plan.
     * Does not consider the timestamp of either plan.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof RegionPlan) {
            final RegionPlan other = (RegionPlan) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(this.getRegion(), other.getRegion()) //
                        && ComparisonUtils.doubleMapEquals2(this.getPlan(), other.getPlan(),
                                ComparisonUtils.WEIGHT_COMPARISON_TOLERANCE);
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + region + " timestamp: " + timestamp + " plan: "
                + plan + " ]";
    }

}
