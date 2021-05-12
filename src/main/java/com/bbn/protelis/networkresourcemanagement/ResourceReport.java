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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.ComparisonUtils;
import com.bbn.protelis.utils.ImmutableUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Information about a node at a particular point in time.
 * 
 * Capacity is a measured value that state how much of a particular resource a
 * server or network link has. The units are specified by the
 * {@link NodeAttribute} or {@link LinkAttribute} that is associated with the
 * value.
 * 
 * Load is a measured value stating how much of a particular resource is being
 * used.
 * 
 * Demand is an estimated value predicting how much a particular resource will
 * be used over the window specified by {@Link #getDemandEstimationWindow()}.
 */
// this annotation is here to allow computed properties to be serialized, but
// not deserialized
@JsonIgnoreProperties(ignoreUnknown = true, value = { "allNetworkDemand", "allNetworkLoad", "allocatedComputeCapacity",
        "averageProcessingTime", "computeDemand", "computeLoad", "containerNetworkDemand",
        "containerNetworkLoad" }, allowGetters = true, allowSetters = false)
public class ResourceReport implements Serializable {

    /**
     * Used to specify the size of the time window that the demand is estimated
     * over. The actual window sizes are application dependent.
     */
    public enum EstimationWindow {
        /**
         * A short window is used.
         */
        SHORT,
        /**
         * A long window is used.
         */
        LONG
    }

    /**
     * This method exists because Protelis cannot access enum constants.
     * 
     * @return {@link EstimationWindow#SHORT}.
     */
    public static final EstimationWindow getShortEstimationWindow() {
        return EstimationWindow.SHORT;
    }

    /**
     * This method exists because Protelis cannot access enum constants.
     * 
     * @return {@link EstimationWindow#LONG}.
     */
    public static final EstimationWindow getLongEstimationWindow() {
        return EstimationWindow.LONG;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp for null reports.
     */
    public static final long NULL_TIMESTAMP = -1;

    /**
     * 
     * @param nodeName
     *            see {@link #getNodeName()}
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param nodeComputeCapacity
     *            see {@link #getNodeComputeCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     * @param networkLoad
     *            see {@link #getNetworkLoad()}
     * @param networkDemand
     *            see {@link #getDemand()}
     * @param demandEstimationWindow
     *            see {@link #getDemandEstimationWindow()}
     * @param containerReports
     *            the reports for the individual containers on this node
     * @throws IllegalArgumentException
     *             if any of the container reports don't have the same demand
     *             estimation window as specified in this constructor
     * @param maximumServiceContainers
     *            see {@link #getMaximumServiceContainers()}
     * @param allocatedServiceContainers
     *            see {@link #getAllocatedServiceContainers()}
     * 
     */
    public ResourceReport(@JsonProperty("nodeName") @Nonnull final NodeIdentifier nodeName,
            @JsonProperty("timestamp") final long timestamp,
            @JsonProperty("demandEstimationWindow") @Nonnull final EstimationWindow demandEstimationWindow,
            @JsonProperty("nodeComputeCapacity") @Nonnull final ImmutableMap<NodeAttribute, Double> nodeComputeCapacity,

            @JsonProperty("networkCapacity") @Nonnull final ImmutableMap<InterfaceIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity,
            @JsonProperty("networkLoad") @Nonnull final ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkLoad,
            @JsonProperty("networkDemand") @Nonnull final ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkDemand,

            @JsonProperty("containerReports") @Nonnull final ImmutableMap<NodeIdentifier, ContainerResourceReport> containerReports,
            @JsonProperty("maximumServiceContainers") final int maximumServiceContainers,
            @JsonProperty("allocatedServiceContainers") final int allocatedServiceContainers) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.nodeComputeCapacity = nodeComputeCapacity;

        this.networkCapacity = networkCapacity;
        this.networkLoad = networkLoad;
        this.networkDemand = networkDemand;

        this.containerReports = containerReports;

        this.maximumServiceContainers = maximumServiceContainers;
        this.allocatedServiceContainers = allocatedServiceContainers;

        // verify everything has the same demand estimation window
        containerReports.forEach((container, report) -> {
            if (!demandEstimationWindow.equals(report.getDemandEstimationWindow())) {
                throw new IllegalArgumentException(
                        "Container report estimation window " + report.getDemandEstimationWindow()
                                + " does not match Resource report estimation window " + demandEstimationWindow);
            }
        });

        // don't include anything that does a fuzzy match in equals
        this.hashCode = Objects.hash(this.nodeName, this.demandEstimationWindow, this.containerReports,
                this.maximumServiceContainers, this.allocatedServiceContainers);
    }

    private final ImmutableMap<NodeIdentifier, ContainerResourceReport> containerReports;

    /**
     * 
     * @return the reports from each container on this node
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ContainerResourceReport> getContainerReports() {
        return containerReports;
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

    private final ImmutableMap<NodeAttribute, Double> nodeComputeCapacity;

    /**
     * Compute capacity for each attribute of a node. The available capacity of
     * the node can be computed by subtracting all of the individual container
     * capacities from this value.
     * 
     * @return Not null.
     * @see #getAllocatedComputeCapacity()
     * @see ContainerResourceReport#getComputeCapacity()
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getNodeComputeCapacity() {
        return nodeComputeCapacity;
    }

    private final ImmutableMap<InterfaceIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity;

    /**
     * The capacity of the links connected to the node. See
     * {@link ContainerResourceReport#getNetworkCapacity()} for a description of
     * the returned map.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<InterfaceIdentifier, ImmutableMap<LinkAttribute, Double>> getNetworkCapacity() {
        return networkCapacity;
    }

    private final ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkLoad;

    /**
     * This is the network traffic passing through this node or terminating at
     * this node.
     * 
     * See {@link ContainerResourceReport#getNetworkLoad()} for a description of
     * the returned map.
     * 
     * @return the network load
     */
    @Nonnull
    public ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>>
            getNetworkLoad() {
        return networkLoad;
    }

    private final ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>> networkDemand;

    /**
     * This is the demand based on {@link #getNetworkLoad()}.
     *
     * See {@link ContainerResourceReport#getNetworkLoad()} for a description of
     * the returned map.
     * 
     * @return the network demand
     */
    @Nonnull
    public ImmutableMap<InterfaceIdentifier, ImmutableMap<RegionNetworkFlow, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute, Double>>>>
            getNetworkDemand() {
        return networkDemand;
    }

    private final EstimationWindow demandEstimationWindow;

    /**
     * @return the window over which the demand values are computed
     * @see #getNetworkDemand()
     * @see #getComputeDemand()
     */
    @Nonnull
    public EstimationWindow getDemandEstimationWindow() {
        return demandEstimationWindow;
    }

    private final NodeIdentifier nodeName;

    /**
     * @return the identifier of the node that the report came from
     */
    @Nonnull
    public final NodeIdentifier getNodeName() {
        return nodeName;
    }

    private transient ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTime = null;

    /**
     * This is computed from the container resource reports.
     * 
     * @return The average time it takes to process a request for each service.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Double> getAverageProcessingTime() {
        if (null == serverAverageProcessingTime) {
            final Map<ServiceIdentifier<?>, Double> rrProcTimeSum = new HashMap<>();
            final Map<ServiceIdentifier<?>, Double> rrProcTimeCount = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ServiceIdentifier<?> service = report.getService();
                if (null != service) {
                    final double time = report.getAverageProcessingTime();
                    if (!Double.isNaN(time)) {
                        rrProcTimeSum.merge(service, time, Double::sum);
                        rrProcTimeCount.merge(service, 1D, Double::sum);
                    }
                }
            });

            ImmutableMap.Builder<ServiceIdentifier<?>, Double> avg = ImmutableMap.builder();
            rrProcTimeSum.forEach((service, sum) -> {
                final double count = rrProcTimeCount.getOrDefault(service, 0D);
                if (count > 0) {
                    avg.put(service, sum / count);
                }
            });
            serverAverageProcessingTime = avg.build();
        }
        return serverAverageProcessingTime;
    }

    /**
     * Get compute load for this node. This is a measured value. service -> node
     * load is coming from -> {@link NodeAttribute} specifying the thing being
     * measured -> value.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getComputeLoad() {
        return getSumContainerComputeLoad();
    }

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>>> computeDemand = null;

    /**
     * Get estimated compute demand for this node. The meanings of the keys and
     * values match those from {@link #getComputeLoad()}, except that this is
     * referring to estimated demand rather than measured load.
     * 
     * @return the demand information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getComputeDemand() {
        if (null == computeDemand) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<NodeIdentifier, Map<NodeAttribute, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> cload = report
                        .getComputeDemand();
                final ServiceIdentifier<?> service = report.getService();
                if (null != service) {
                    final Map<NodeIdentifier, Map<NodeAttribute, Double>> serviceLoad = sload.computeIfAbsent(service,
                            k -> new HashMap<>());

                    cload.forEach((srcNode, values) -> {
                        final Map<NodeAttribute, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcNode,
                                k -> new HashMap<>());
                        values.forEach((attr, value) -> {
                            sRegionLoad.merge(attr, value, Double::sum);
                        });
                    });
                }
            });

            computeDemand = ImmutableUtils.makeImmutableMap3(sload);
        }
        return computeDemand;
    }

    /**
     * The sum of the compute capacity for each running container. By comparing
     * this with {@link #getNodeComputeCapacity()} one can determine the
     * available capacity.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getAllocatedComputeCapacity() {
        return getSumContainerCapacity();
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

    /**
     * Create a resource report with no data. The timestamp is set to
     * {@link #NULL_TIMESTAMP}.
     * 
     * @param nodeName
     *            the name of the node
     * @return empty report for a node
     * @param demandWindow
     *            the estimation window for this null report
     */
    public static ResourceReport getNullReport(@Nonnull final NodeIdentifier nodeName,
            @Nonnull final ResourceReport.EstimationWindow demandWindow) {

        return new ResourceReport(nodeName, NULL_TIMESTAMP, demandWindow, //
                ImmutableMap.of(), // nodeComputeCapacity

                ImmutableMap.of(), // networkCapacity
                ImmutableMap.of(), // networkLoad
                ImmutableMap.of(), // networkDemand

                ImmutableMap.of(), // container reports
                0, 0);
    }

    @Override
    public String toString() {
        return "{" + " node: " + getNodeName() + " nodeComputeCapacity: " + getNodeComputeCapacity()
                + " allocatedComputeCapacity: " + getAllocatedComputeCapacity() + " computeLoad: " + getComputeLoad()
                + " containerReports: " + getContainerReports() + "}";
    }

    private transient ImmutableMap<NodeAttribute, Double> sumContainerCapacity = null;

    @Nonnull
    private ImmutableMap<NodeAttribute, Double> getSumContainerCapacity() {
        if (null == sumContainerCapacity) {
            // compute it
            final Map<NodeAttribute, Double> result = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeAttribute, Double> cValue = report.getComputeCapacity();
                cValue.forEach((attr, value) -> {
                    result.merge(attr, value, Double::sum);
                });
            });

            sumContainerCapacity = ImmutableMap.copyOf(result);
        }
        return sumContainerCapacity;
    }

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>>> sumContainerComputeLoad = null;

    @Nonnull
    private ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getSumContainerComputeLoad() {
        if (null == sumContainerComputeLoad) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<NodeIdentifier, Map<NodeAttribute, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute, Double>> cload = report.getComputeLoad();
                final ServiceIdentifier<?> service = report.getService();
                if (null != service) {
                    final Map<NodeIdentifier, Map<NodeAttribute, Double>> serviceLoad = sload.computeIfAbsent(service,
                            k -> new HashMap<>());

                    cload.forEach((srcNode, values) -> {
                        final Map<NodeAttribute, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcNode,
                                k -> new HashMap<>());
                        values.forEach((attr, value) -> {
                            sRegionLoad.merge(attr, value, Double::sum);
                        });
                    });
                }
            });

            sumContainerComputeLoad = ImmutableUtils.makeImmutableMap3(sload);
        }
        return sumContainerComputeLoad;
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
            final ResourceReport other = (ResourceReport) o;

            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(this.nodeName, other.nodeName) //
                        && Objects.equals(this.demandEstimationWindow, other.demandEstimationWindow) //
                        && this.maximumServiceContainers == other.maximumServiceContainers //
                        && this.allocatedServiceContainers == other.allocatedServiceContainers //
                        && ComparisonUtils.doubleMapEquals(this.nodeComputeCapacity, other.nodeComputeCapacity,
                                ComparisonUtils.NODE_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals2(this.networkCapacity, other.networkCapacity,
                                ComparisonUtils.LINK_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && ComparisonUtils.doubleMapEquals4(this.networkLoad, other.networkLoad,
                                ComparisonUtils.LINK_ATTRIBUTE_COMPARISON_TOLERANCE) //
                        && Objects.equals(this.containerReports, other.containerReports) //
                ;
            }
        } else {
            return false;
        }
    }

}
