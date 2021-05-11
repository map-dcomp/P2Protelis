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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Summary information about the resources in a region. See
 * {@link ResourceReport} for information about capacity vs. load vs. demand.
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param region
     *            see {@link ResourceSummary#getRegion()}
     * @param serverLoad
     *            see {@link #getServerLoad()}
     * @param serverCapacity
     *            see {@link #getServerCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     * @param networkLoad
     *            see {@link #getNetworkLoad()}
     * @param networkDemand
     *            see {@link #getNetworkDemand()}
     * @param serverDemand
     *            see {@link #getServerDemand()}
     * @param minTimestamp
     *            see {@link #getMinTimestamp()}
     * @param maxTimestamp
     *            see {@link #getMaxTimestamp()}
     * @param demandEstimationWindow
     *            see {@link #getDemandEstimationWindow()}
     * @param serverAverageProcessingTime
     *            Used to compute {@link #getServerAverageProcessingTime()}
     * @param maximumServiceContainers
     *            see {@link #getMaximumServiceContainers()}
     * @param allocatedServiceContainers
     *            see {@link #getAllocatedServiceContainers()}
     * 
     * 
     */
    public ResourceSummary(@JsonProperty("region") @Nonnull final RegionIdentifier region,
            @JsonProperty("minTimestamp") final long minTimestamp,
            @JsonProperty("maxTimestamp") final long maxTimestamp,
            @JsonProperty("demandEstimationWindow") @Nonnull final ResourceReport.EstimationWindow demandEstimationWindow,

            @JsonProperty("serverCapacity") @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @JsonProperty("serverLoad") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverLoad,
            @JsonProperty("serverDemand") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverDemand,

            @JsonProperty("serverAverageProcessingTime") @Nonnull final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTime,

            @JsonProperty("networkCapacity") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity,
            @JsonProperty("networkLoad") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkLoad,
            @JsonProperty("networkDemand") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkDemand,
            @JsonProperty("maximumServiceContainers") final int maximumServiceContainers,
            @JsonProperty("allocatedServiceContainers") final int allocatedServiceContainers) {
        this.region = region;
        this.minTimestamp = minTimestamp;
        this.maxTimestamp = maxTimestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.serverLoad = serverLoad;
        this.serverDemand = serverDemand;
        this.serverCapacity = serverCapacity;
        this.serverAverageProcessingTime = serverAverageProcessingTime;

        this.networkCapacity = networkCapacity;
        this.networkLoad = networkLoad;
        this.networkDemand = networkDemand;

        this.maximumServiceContainers = maximumServiceContainers;
        this.allocatedServiceContainers = allocatedServiceContainers;

        // don't include anything that does a fuzzy match in equals
        this.hashCode = Objects.hash(this.region, this.minTimestamp, this.maxTimestamp, this.demandEstimationWindow,
                this.maximumServiceContainers, this.allocatedServiceContainers);
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this summary information is for
     */
    @Nonnull
    public final RegionIdentifier getRegion() {
        return region;
    }

    private final long minTimestamp;

    /**
     * @return the minimum timestamp of the reports combined to create this
     *         summary
     * @see ResourceReport#getTimestamp()
     */
    public long getMinTimestamp() {
        return minTimestamp;
    }

    private final long maxTimestamp;

    /**
     * @return the maximum timestamp of the reports combined to create this
     *         summary
     * @see ResourceReport#getTimestamp()
     */
    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    private final ResourceReport.EstimationWindow demandEstimationWindow;

    /**
     * @return the window over which the demand values are computed
     * @see #getServerDemand()
     * @see #getNetworkDemand()
     */
    @Nonnull
    public ResourceReport.EstimationWindow getDemandEstimationWindow() {
        return demandEstimationWindow;
    }

    private final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTime;

    /**
     * 
     * @return the average processing time for services in this region
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Double> getServerAverageProcessingTime() {
        return serverAverageProcessingTime;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverLoad;

    /**
     * Get server load for this region. This is a measured value. service ->
     * source region of the load -> measured attribute -> value.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getComputeLoad()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getServerLoad() {
        return serverLoad;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverDemand;

    /**
     * Get server estimated demand for this region. service -> source region for
     * the demand -> attribute -> value.
     * 
     * @return the demand information. Not null.
     * @see ResourceReport#getComputeDemand()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getServerDemand() {
        return serverDemand;
    }

    private final ImmutableMap<NodeAttribute, Double> serverCapacity;

    /**
     * Server capacity for this region.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getNodeComputeCapacity()
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity;

    /**
     * Network capacity for neighboring regions. neighbor region -> attribute ->
     * value. Only direct neighbors are reported.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getNetworkCapacity()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> getNetworkCapacity() {
        return networkCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkLoad;

    /**
     * Network load and where it comes from. neighbor region -> flow data ->
     * service -> attribute -> value.
     * 
     * Only direct neighbors are reported.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getNetworkLoad()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>>
            getNetworkLoad() {
        return networkLoad;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkDemand;

    /**
     * Network demand for neighboring regions. Only direct neighbors are
     * reported. See {@link #getNetworkLoad()} for a description of the map.
     * 
     * @return the demand information. Not null.
     * @see ResourceReport#getNetworkDemand()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>>
            getNetworkDemand() {
        return networkDemand;
    }

    private final int maximumServiceContainers;

    /**
     * 
     * @return the maximum number of service containers that can run on this
     *         node.
     */
    public int getMaximumServiceContainers() {
        return maximumServiceContainers;
    }

    private final int allocatedServiceContainers;

    /**
     * 
     * @return the number of service containers that are currently allocated
     */
    public int getAllocatedServiceContainers() {
        return allocatedServiceContainers;
    }

    /**
     * 
     * @return the number of service containers that can be added to this node
     */
    public int getAvailableServiceContainers() {
        return maximumServiceContainers - allocatedServiceContainers;
    }

    @Override
    public String toString() {
        return "{" + " region: " + getRegion() + " serverLoad: " + getServerLoad() + "}";
    }

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (this.getClass().equals(o.getClass())) {
            final ResourceSummary other = (ResourceSummary) o;

            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(this.region, other.region) //
                        && this.minTimestamp == other.minTimestamp //
                        && this.maxTimestamp == other.maxTimestamp //
                        && this.maximumServiceContainers == other.maximumServiceContainers //
                        && this.allocatedServiceContainers == other.allocatedServiceContainers //
                        && Objects.equals(this.demandEstimationWindow, other.demandEstimationWindow) //

                        && ComparisonUtils.doubleMapEquals3(this.serverLoad, other.serverLoad,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals3(this.serverDemand, other.serverDemand,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals(this.serverCapacity, other.serverCapacity,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals(this.serverAverageProcessingTime,
                                other.serverAverageProcessingTime, ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //

                        && ComparisonUtils.doubleMapEquals2(this.networkCapacity, other.networkCapacity,
                                ComparisonUtils.LINK_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals4(this.networkLoad, other.networkLoad,
                                ComparisonUtils.LINK_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals4(this.networkDemand, other.networkDemand,
                                ComparisonUtils.LINK_ATTRIBUTE_COMPARISON_TOLERANCE) //
                ;
            }
        } else {
            return false;
        }
    }

}
