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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

/**
 * Plan for balancing services in a region. The load balancer should look at the
 * {@link ResourceReport} and {@link RegionPlan} objects for the current region
 * to come up with a detailed plan of which services should run on which nodes.
 */
public class LoadBalancerPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp for null reports.
     */
    public static final long NULL_TIMESTAMP = -1;

    /**
     * This is used as the default plan by Protelis.
     * 
     * @param region
     *            see {@link #getRegion()}
     * @return empty plan for a region
     */
    @Nonnull
    public static LoadBalancerPlan getNullLoadBalancerPlan(@Nonnull final RegionIdentifier region) {
        return new LoadBalancerPlan(region, NULL_TIMESTAMP, ImmutableMap.of(), // servicePlan
                ImmutableMap.of()); // overflowPlan
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param servicePlan
     *            see {@link #getServicePlan()}
     * @param overflowPlan
     *            see {@link #getOverflowPlan()}
     */
    public LoadBalancerPlan(@JsonProperty("region") @Nonnull final RegionIdentifier region,
            @JsonProperty("timestamp") @Nonnull final long timestamp,
            @JsonProperty("servicePlan") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableCollection<ContainerInfo>> servicePlan,
            @JsonProperty("overflowPlan") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan) {
        this.regionName = region;
        this.timestamp = timestamp;
        this.servicePlan = servicePlan;
        this.overflowPlan = overflowPlan;
        // don't include things that use a fuzzy match in equals
        this.hashCode = Objects.hash(regionName, this.servicePlan);
    }

    /**
     * 
     * @param region
     *            see {@link #getRegion()}
     * @param servicePlan
     *            see {@link #getServicePlan()}
     * @param overflowPlan
     *            see {@link #getOverflowPlan()}
     */
    public LoadBalancerPlan(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableCollection<ContainerInfo>> servicePlan,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan) {
        this.regionName = region;
        this.servicePlan = servicePlan;
        this.overflowPlan = overflowPlan;
        this.hashCode = Objects.hash(regionName, this.servicePlan, this.overflowPlan);
    }

    private final RegionIdentifier regionName;

    /**
     * @return the region that this plan is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return this.regionName;
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

    private final ImmutableMap<NodeIdentifier, ImmutableCollection<ContainerInfo>> servicePlan;

    /**
     * Plan for which services should run on which nodes. See
     * {@link ContainerInfo} for details on what the properties mean.
     * 
     * @return the plan
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableCollection<ContainerInfo>> getServicePlan() {
        return servicePlan;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan;

    /**
     * Plan for handling overflow. This is usually based on
     * {@link RegionPlan#getPlan()} and the current load in the region.
     * 
     * An empty map means all traffic stays local if there are containers
     * started in the current region, otherwise the traffic all goes to the
     * default region for the service. If the overflow plan is populated and the
     * current region isn't included in the plan, this means that ALL traffic
     * should be sent to other regions.
     * 
     * @return the plan. service -> region to send traffic to -> value, ideally
     *         a value between 0 and 1 that represents a percentage of traffic
     *         to send to the other region
     * @see RegionPlan#getPlan()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> getOverflowPlan() {
        return overflowPlan;
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
        } else if (o instanceof LoadBalancerPlan) {
            final LoadBalancerPlan other = (LoadBalancerPlan) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(getRegion(), other.getRegion())
                        && Objects.equals(getServicePlan(), other.getServicePlan())
                        && ComparisonUtils.doubleMapEquals2(this.getOverflowPlan(), other.getOverflowPlan(),
                                ComparisonUtils.WEIGHT_COMPARISON_TOLERANCE);
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " region: " + regionName //
                + " timestamp: " + timestamp //
                + " servicePlan: " + servicePlan //
                + " overflowPlan: " + overflowPlan //
                + " ]";
    }

    /**
     * Information about a container in the plan.
     */
    public static final class ContainerInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 
         * @param id
         *            see {@link #getId()}
         * @param service
         *            see {@link #getService()}
         * @param weight
         *            see {@link #getWeight()}, must be a value greater than 0
         * @param stopTrafficTo
         *            see {@link #isStopTrafficTo()}
         * @param stop
         *            see {@link #isStop()}
         * @throws IllegalArgumentException
         *             if the weight is not greater than 0 and stopTrafficTo and
         *             stop are false
         */
        public ContainerInfo(@JsonProperty("id") final NodeIdentifier id,
                @JsonProperty("service") @Nonnull final ServiceIdentifier<?> service,
                @JsonProperty("weight") final double weight,
                @JsonProperty("stopTrafficTo") final boolean stopTrafficTo,
                @JsonProperty("stop") final boolean stop) {
            if (weight <= 0 && !stop && !stopTrafficTo) {
                throw new IllegalArgumentException(
                        "Container weight must be greater than 0 if the container is to receive any traffic");
            }

            this.id = id;
            this.service = service;
            this.weight = weight;
            this.stopTrafficTo = stopTrafficTo;
            this.stop = stop;
            // don't include things that have a fuzzy match in equals
            this.hashCode = Objects.hash(this.id, this.service);
        }

        private final NodeIdentifier id;

        /**
         * @return The ID of the container or null. If null, this is a new
         *         container to be started.
         */
        public NodeIdentifier getId() {
            return id;
        }

        private final ServiceIdentifier<?> service;

        /**
         * 
         * @return the service to run on the container
         */
        @Nonnull
        public ServiceIdentifier<?> getService() {
            return service;
        }

        private final double weight;

        /**
         * 
         * @return the weight of this container, used to determine how much this
         *         container should be used relative to other containers running
         *         the same service in the region
         */
        public double getWeight() {
            return weight;
        }

        private final boolean stopTrafficTo;

        /**
         * 
         * @return if true, then don't send any traffic to this container
         */
        public boolean isStopTrafficTo() {
            return stopTrafficTo;
        }

        private final boolean stop;

        /**
         * 
         * @return if true stop this container
         */
        public boolean isStop() {
            return stop;
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
            } else if (this == o) {
                return true;
            } else if (this.getClass().equals(o.getClass())) {
                final ContainerInfo other = (ContainerInfo) o;
                if (this.hashCode != other.hashCode) {
                    return false;
                } else {
                    return Objects.equals(getId(), other.getId()) //
                            && Objects.equals(getService(), other.getService()) //
                            && Math.abs(getWeight() - other.getWeight()) < ComparisonUtils.WEIGHT_COMPARISON_TOLERANCE
                            && isStop() == other.isStop() //
                            && isStopTrafficTo() == other.isStopTrafficTo();
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " [" //
                    + " container: " + id //
                    + " service: " + service //
                    + " weight: " + weight //
                    + " stopTrafficTo: " + stopTrafficTo //
                    + " stop: " + stop //
                    + " ]";
        }

    }
}
