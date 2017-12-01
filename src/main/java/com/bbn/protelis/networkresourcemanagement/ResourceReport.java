package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.ImmutableUtils;
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
     * @param nodeNetworkCapacity
     *            see {@link #getNodeNetworkCapacity()}
     * @param demandEstimationWindow
     *            see {#link {@link #getDemandEstimationWindow()}
     * @param containerReports
     *            the reports for the individual containers on this node
     * @throws IllegalArgumentException
     *             if any of the container reports don't have the same demand
     *             estimation window as specified in this constructor
     */
    public ResourceReport(@Nonnull final NodeIdentifier nodeName,
            final long timestamp,
            @Nonnull final EstimationWindow demandEstimationWindow,
            @Nonnull final ImmutableMap<NodeAttribute<?>, Double> nodeComputeCapacity,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkCapacity,
            @Nonnull final ImmutableMap<ContainerIdentifier, ContainerResourceReport> containerReports) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.nodeComputeCapacity = nodeComputeCapacity;
        this.nodeNetworkCapacity = nodeNetworkCapacity;
        this.containerReports = containerReports;

        // verify everything has the same demand estimation window
        containerReports.forEach((container, report) -> {
            if (!demandEstimationWindow.equals(report.getDemandEstimationWindow())) {
                throw new IllegalArgumentException(
                        "Container report estimation window " + report.getDemandEstimationWindow()
                                + " does not match Resource report estimation window " + demandEstimationWindow);
            }
        });
    }

    private final ImmutableMap<ContainerIdentifier, ContainerResourceReport> containerReports;

    /**
     * 
     * @return the reports from each container on this node
     */
    @Nonnull
    public ImmutableMap<ContainerIdentifier, ContainerResourceReport> getContainerReports() {
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

    private final ImmutableMap<NodeAttribute<?>, Double> nodeComputeCapacity;

    /**
     * Compute capacity for each attribute of a node. The available capacity of
     * the node can be computed by subtracting all of the individual container
     * capacities from this value.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getNodeComputeCapacity() {
        return nodeComputeCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkCapacity;

    /**
     * Node link capacity for neighboring nodes. neighbor node -> attribute ->
     * value. Each key in the list is the identifier of a neighboring node. The
     * available capacity of the node can be computed by subtracting all of the
     * individual container capacities from this value.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNetworkCapacity() {
        return nodeNetworkCapacity;
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
     * 
     * @return the average time it takes to process a request for each service
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Double> getAverageProcessingTime() {
        if (null == serverAverageProcessingTime) {
            final Map<ServiceIdentifier<?>, Double> rrProcTimeSum = new HashMap<>();
            final Map<ServiceIdentifier<?>, Double> rrProcTimeCount = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ServiceIdentifier<?> service = report.getService();
                final double time = report.getAverageProcessingTime();

                rrProcTimeSum.merge(service, time, Double::sum);
                rrProcTimeCount.merge(service, 1D, Double::sum);
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

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> computeLoad = null;

    /**
     * Get compute load for this node. This is a measured value. service ->
     * region load is coming from -> {@link NodeAttribute} specifying the thing
     * being measured -> value.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getComputeLoad() {
        if (null == computeLoad) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<RegionIdentifier, Map<NodeAttribute<?>, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>> cload = report
                        .getComputeLoad();
                final Map<RegionIdentifier, Map<NodeAttribute<?>, Double>> serviceLoad = sload
                        .computeIfAbsent(report.getService(), k -> new HashMap<>());

                cload.forEach((srcRegion, values) -> {
                    final Map<NodeAttribute<?>, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcRegion,
                            k -> new HashMap<>());
                    values.forEach((attr, value) -> {
                        sRegionLoad.merge(attr, value, Double::sum);
                    });
                });
            });

            computeLoad = ImmutableUtils.makeImmutableMap3(sload);
        }
        return computeLoad;
    }

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> computeDemand = null;

    /**
     * Get estimated compute demand for this node. The meanings of the keys and
     * values match those from {@link #getComputeLoad()}, except that this is
     * referring to estimated demand rather than measured load.
     * 
     * @return the demand information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getComputeDemand() {
        if (null == computeDemand) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<RegionIdentifier, Map<NodeAttribute<?>, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>> cload = report
                        .getComputeDemand();
                final Map<RegionIdentifier, Map<NodeAttribute<?>, Double>> serviceLoad = sload
                        .computeIfAbsent(report.getService(), k -> new HashMap<>());

                cload.forEach((srcRegion, values) -> {
                    final Map<NodeAttribute<?>, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcRegion,
                            k -> new HashMap<>());
                    values.forEach((attr, value) -> {
                        sRegionLoad.merge(attr, value, Double::sum);
                    });
                });
            });

            computeDemand = ImmutableUtils.makeImmutableMap3(sload);
        }
        return computeDemand;
    }

    private transient ImmutableMap<NodeAttribute<?>, Double> allocatedComputeCapacity = null;

    /**
     * The sum of the compute capacity for each running container. By comparing
     * this with {@link #getNodeComputeCapacity()} one can determine the
     * available capacity.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getAllocatedComputeCapacity() {
        if (null == allocatedComputeCapacity) {
            final Map<NodeAttribute<?>, Double> rrCapacity = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeAttribute<?>, Double> cCapacity = report.getComputeCapacity();
                cCapacity.forEach((attr, value) -> {
                    rrCapacity.merge(attr, value, Double::sum);
                });
            });
            allocatedComputeCapacity = ImmutableMap.copyOf(rrCapacity);
        }
        return allocatedComputeCapacity;
    }

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> allocatedNetworkCapacity = null;

    /**
     * The sum of the container link capacity for neighboring nodes. neighbor
     * node -> attribute -> value. Each key in the list is the identifier of a
     * neighboring node. By comparing this with
     * {@link #getNodeNetworkCapacity()} one can determine the available
     * capacity.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkCapacity() {
        if (null == allocatedNetworkCapacity) {
            final Map<NodeIdentifier, Map<LinkAttribute<?>, Double>> rrCapacity = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> cCapacity = report
                        .getNetworkCapacity();

                cCapacity.forEach((neighbor, values) -> {
                    final Map<LinkAttribute<?>, Double> rrNeighborCapacity = rrCapacity.computeIfAbsent(neighbor,
                            k -> new HashMap<>());

                    values.forEach((attr, value) -> {
                        rrNeighborCapacity.merge(attr, value, Double::sum);
                    });
                });
            });
            allocatedNetworkCapacity = ImmutableUtils.makeImmutableMap2(rrCapacity);
        }
        return allocatedNetworkCapacity;
    }

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad = null;

    /**
     * Network load to neighboring nodes. See {@link #getNetworkCapacity()} for
     * details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkLoad() {
        if (null == networkLoad) {
            // compute it
            final Map<NodeIdentifier, Map<LinkAttribute<?>, Double>> nload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> cload = report
                        .getNetworkLoad();

                cload.forEach((neighborNode, values) -> {

                    final Map<LinkAttribute<?>, Double> rrValues = nload.computeIfAbsent(neighborNode,
                            k -> new HashMap<>());

                    values.forEach((attr, value) -> {
                        rrValues.merge(attr, value, Double::sum);
                    });
                });
            });

            networkLoad = ImmutableUtils.makeImmutableMap2(nload);
        }
        return networkLoad;
    }

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand = null;

    /**
     * Network demand to neighboring nodes. See {@link #getNetworkCapacity()}
     * for details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkDemand() {
        if (null == networkDemand) {
            // compute it
            final Map<NodeIdentifier, Map<LinkAttribute<?>, Double>> nload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> cload = report
                        .getNetworkDemand();

                cload.forEach((neighborNode, values) -> {

                    final Map<LinkAttribute<?>, Double> rrValues = nload.computeIfAbsent(neighborNode,
                            k -> new HashMap<>());

                    values.forEach((attr, value) -> {
                        rrValues.merge(attr, value, Double::sum);
                    });
                });
            });

            networkDemand = ImmutableUtils.makeImmutableMap2(nload);
        }
        return networkDemand;
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
                ImmutableMap.of(), // nodeServerCapacity
                ImmutableMap.of(), // nodeNetworkCapacity
                ImmutableMap.of()); // container reports
    }

}
