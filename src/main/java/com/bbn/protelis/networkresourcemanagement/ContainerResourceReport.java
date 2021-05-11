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
 * Information about a container at a particular point in time.
 * 
 * See {@link ResourceReport} for details about load, capacity and demand.
 */
public class ContainerResourceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp for null reports.
     */
    public static final long NULL_TIMESTAMP = -1;

    /**
     * 
     * @param containerName
     *            see {@link #getContainerName()}
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param computeLoad
     *            see {@link #getComputeLoad()}
     * @param computeCapacity
     *            see {@link #getComputeCapacity()}
     * @param demandEstimationWindow
     *            see {#link {@link #getDemandEstimationWindow()}
     * @param computeDemand
     *            see {@link #getComputeDemand()}
     * @param serverAverageProcessingTime
     *            see {@link #getAverageProcessingTime()}
     * @param service
     *            see {@link #getService()}
     * @param serviceStatus
     *            see {@link #getServiceStatus()}
     */
    public ContainerResourceReport(@JsonProperty("containerName") @Nonnull final NodeIdentifier containerName,
            @JsonProperty("timestamp") final long timestamp,

            @JsonProperty("service") final ServiceIdentifier<?> service,
            @JsonProperty("serviceStatus") @Nonnull final ServiceStatus serviceStatus,

            @JsonProperty("demandEstimationWindow") @Nonnull final ResourceReport.EstimationWindow demandEstimationWindow,
            @JsonProperty("computeCapacity") @Nonnull final ImmutableMap<NodeAttribute, Double> computeCapacity,
            @JsonProperty("computeLoad") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> computeLoad,
            @JsonProperty("computeDemand") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> computeDemand,
            @JsonProperty("averageProcessingTime") final double serverAverageProcessingTime) {
        this.containerName = containerName;
        this.timestamp = timestamp;

        this.service = service;
        this.serviceStatus = Objects.requireNonNull(serviceStatus);

        this.demandEstimationWindow = demandEstimationWindow;

        this.computeLoad = computeLoad;
        this.computeCapacity = computeCapacity;
        this.computeDemand = computeDemand;
        this.averageProcessingTime = serverAverageProcessingTime;

        // don't include anything that does a fuzzy match in equals
        this.hashCode = Objects.hash(this.containerName, this.service, this.serviceStatus, this.demandEstimationWindow);
    }

    private final ServiceStatus serviceStatus;

    /**
     * 
     * @return the current status of the service
     */
    @Nonnull
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    private final ServiceIdentifier<?> service;

    /**
     * 
     * @return the service running in the container, null if no service is
     *         currently running
     */
    public ServiceIdentifier<?> getService() {
        return service;
    }

    private final long timestamp;

    /**
     * The units of the timestamp are determined by the clock used for the
     * network. Possible examples may be milliseconds since the epoch or
     * milliseconds since the start of the application. It is not expected that
     * this time be converted to a date time for display to the user. This value
     * is used to differentiate 2 reports for the same node taken at different
     * times.
     * 
     * @return when the report was generated
     */
    public long getTimestamp() {
        return timestamp;
    }

    private final ResourceReport.EstimationWindow demandEstimationWindow;

    /**
     * @return the window over which the demand values are computed
     * @see #getComputeDemand()
     */
    @Nonnull
    public ResourceReport.EstimationWindow getDemandEstimationWindow() {
        return demandEstimationWindow;
    }

    private final NodeIdentifier containerName;

    /**
     * @return the identifier of the container that the report came from
     */
    @Nonnull
    public final NodeIdentifier getContainerName() {
        return containerName;
    }

    private final double averageProcessingTime;

    /**
     * If no requests have completed for the service in this container this
     * value will be NaN.
     * 
     * @return The average time it takes to process a request for the service.
     */
    @Nonnull
    public double getAverageProcessingTime() {
        return averageProcessingTime;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> computeLoad;

    /**
     * Get compute load for this node. This is a measured value. node load is
     * coming from -> {@link NodeAttribute} specifying the thing being measured
     * -> value.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> getComputeLoad() {
        return computeLoad;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> computeDemand;

    /**
     * Get estimated compute demand for this node. The meanings of the keys and
     * values match those from {@link #getComputeLoad()}, except that this is
     * referring to estimated demand rather than measured load.
     * 
     * @return the demand information. Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> getComputeDemand() {
        return computeDemand;
    }

    private final ImmutableMap<NodeAttribute, Double> computeCapacity;

    /**
     * Compute capacity for each attribute of a node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getComputeCapacity() {
        return computeCapacity;
    }

    /**
     * Create a container resource report with no data. The timestamp is set to
     * {@link #NULL_TIMESTAMP}.
     * 
     * @param containerName
     *            the name of the node
     * @return empty report for a node
     * @param demandWindow
     *            the estimation window for this null report
     */
    public static ContainerResourceReport getNullReport(@Nonnull final NodeIdentifier containerName,
            @Nonnull final ResourceReport.EstimationWindow demandWindow) {

        return new ContainerResourceReport(containerName, NULL_TIMESTAMP, //
                null, // service
                ServiceStatus.STOPPED, //
                demandWindow, //
                ImmutableMap.of(), // serverCapacity
                ImmutableMap.of(), // serverLoad
                ImmutableMap.of(), // serverDemand
                0 // serverAverageProcessingTime
        );

    }

    @Override
    public String toString() {
        return "{" + " computeCapacity: " + getComputeCapacity() + " computeLoad: " + getComputeLoad()
                + " computeDemand: " + getComputeDemand() + " }";
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
        } else if (null == o) {
            return false;
        } else if (getClass().equals(o.getClass())) {
            final ContainerResourceReport other = (ContainerResourceReport) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(this.containerName, other.containerName) //
                        && Objects.equals(this.service, other.service) //
                        && Objects.equals(this.serviceStatus, other.serviceStatus) //
                        && Objects.equals(this.demandEstimationWindow, other.demandEstimationWindow) //
                        && ComparisonUtils.doubleMapEquals(this.computeCapacity, other.computeCapacity,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals2(this.computeLoad, other.computeLoad,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE)//
                        && ComparisonUtils.doubleMapEquals2(this.computeDemand, other.computeDemand,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE)//
                ;
            }
        } else {
            return false;
        }
    }

}
