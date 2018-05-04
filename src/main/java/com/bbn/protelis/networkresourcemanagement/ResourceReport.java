package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

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
     * @param nodeNetworkCapacity
     *            see {@link #getNodeNetworkCapacity()}
     * @param nodeNetworkLoad
     *            see {@link #getNodeNetworkLoad()}
     * @param nodeNetworkDemand
     *            see {@link #getNodeNetworkDemand()}
     * @param nodeNeighborNetworkCapacity
     *            see {@link #getNodeNeighborNetworkCapacity()}
     * @param nodeNeighborNetworkLoad
     *            see {@link #getNodeNeighborNetworkLoad()}
     * @param nodeNeighborNetworkDemand
     *            see {@link #getNodeNeighborNetworkDemand()}
     * @param demandEstimationWindow
     *            see {@link #getDemandEstimationWindow()}
     * @param containerReports
     *            the reports for the individual containers on this node
     * @throws IllegalArgumentException
     *             if any of the container reports don't have the same demand
     *             estimation window as specified in this constructor
     */
    public ResourceReport(@JsonProperty("nodeName") @Nonnull final NodeIdentifier nodeName,
            @JsonProperty("timestamp") final long timestamp,
            @JsonProperty("demandEstimationWindow") @Nonnull final EstimationWindow demandEstimationWindow,
            @JsonProperty("nodeComputeCapacity") @Nonnull final ImmutableMap<NodeAttribute<?>, Double> nodeComputeCapacity,

            @JsonProperty("nodeNetworkCapacity") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkCapacity,
            @JsonProperty("nodeNetworkLoad") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkLoad,
            @JsonProperty("nodeNetworkDemand") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkDemand,

            @JsonProperty("nodeNeighborNetworkCapacity") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkCapacity,
            @JsonProperty("nodeNeighborNetworkLoad") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkLoad,
            @JsonProperty("nodeNeighborNetworkDemand") @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkDemand,

            @JsonProperty("containerReports") @Nonnull final ImmutableMap<ContainerIdentifier, ContainerResourceReport> containerReports) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.nodeComputeCapacity = nodeComputeCapacity;

        this.nodeNetworkCapacity = nodeNetworkCapacity;
        this.nodeNetworkLoad = nodeNetworkLoad;
        this.nodeNetworkDemand = nodeNetworkDemand;

        this.nodeNeighborNetworkCapacity = nodeNeighborNetworkCapacity;
        this.nodeNeighborNetworkLoad = nodeNeighborNetworkLoad;
        this.nodeNeighborNetworkDemand = nodeNeighborNetworkDemand;

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
     * This is the same as the property
     * {@link ContainerResourceReport#getNetworkCapacity()}, except it's for the
     * node itself rather than a container on the node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNetworkCapacity() {
        return nodeNetworkCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkLoad;

    /**
     * This is the same as the property
     * {@link ContainerResourceReport#getNetworkLoad()}, except it's for the
     * node itself rather than a container on the node.
     * 
     * @return the network load to this node and not the containers
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNetworkLoad() {
        return nodeNetworkLoad;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNetworkDemand;

    /**
     * This is the same as the property
     * {@link ContainerResourceReport#getNetworkDemand()}, except it's for the
     * node itself rather than a container on the node.
     * 
     * @return the network demand that isn't attributed to a container
     * @see #getNodeNetworkLoad()
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNetworkDemand() {
        return nodeNetworkDemand;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkCapacity;

    /**
     * This is the same as the property
     * {@link ContainerResourceReport#getNeighborNetworkCapacity()}, except it's
     * for the node itself rather than a container on the node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNeighborNetworkCapacity() {
        return nodeNeighborNetworkCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkLoad;

    /**
     * This is the same as the property
     * {@link ContainerResourceReport#getNeighborNetworkLoad()}, except it's for
     * the node itself rather than a container on the node.
     * 
     * @return the network load passing through this node
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNeighborNetworkLoad() {
        return nodeNeighborNetworkLoad;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> nodeNeighborNetworkDemand;

    /**
     * This is the same as the property
     * {@link ContainerResourceReport#getNeighborNetworkDemand()}, except it's
     * for the node itself rather than a container on the node.
     * 
     * @return the network demand that isn't attributed to a container
     * @see #getNodeNetworkLoad()
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNodeNeighborNetworkDemand() {
        return nodeNeighborNetworkDemand;
    }

    private final EstimationWindow demandEstimationWindow;

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> allNetworkLoad = null;

    /**
     * The sum of {@link #getNodeNetworkLoad()} and
     * {@link ContainerResourceReport#getNetworkLoad()} for all containers.
     * 
     * @return all network load on the node and it's containers
     */
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getAllNetworkLoad() {
        if (null == allNetworkLoad) {
            final Map<NodeIdentifier, Map<LinkAttribute<?>, Double>> nload = new HashMap<>();

            getNodeNetworkLoad().forEach((srcNode, load) -> {
                final Map<LinkAttribute<?>, Double> reportLoad = nload.computeIfAbsent(srcNode, k -> new HashMap<>());
                load.forEach((attr, value) -> {
                    reportLoad.merge(attr, value, Double::sum);
                });
            });

            getContainerReports().forEach((cid, creport) -> {
                creport.getNetworkLoad().forEach((srcNode, load) -> {
                    final Map<LinkAttribute<?>, Double> reportLoad = nload.computeIfAbsent(srcNode,
                            k -> new HashMap<>());
                    load.forEach((attr, value) -> {
                        reportLoad.merge(attr, value, Double::sum);
                    });
                });
            });

            allNetworkLoad = ImmutableUtils.makeImmutableMap2(nload);
        }

        return allNetworkLoad;
    }

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> allNetworkDemand = null;

    /**
     * The sum of {@link #getNodeNetworkDemand()} and
     * {@link ContainerResourceReport#getNetworkDemand()} for all containers.
     * 
     * @return all network load on the node and it's containers
     */
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getAllNetworkDemand() {
        if (null == allNetworkDemand) {
            final Map<NodeIdentifier, Map<LinkAttribute<?>, Double>> nload = new HashMap<>();

            getNodeNetworkDemand().forEach((srcNode, load) -> {
                final Map<LinkAttribute<?>, Double> reportLoad = nload.computeIfAbsent(srcNode, k -> new HashMap<>());
                load.forEach((attr, value) -> {
                    reportLoad.merge(attr, value, Double::sum);
                });
            });

            getContainerReports().forEach((cid, creport) -> {
                creport.getNetworkDemand().forEach((srcNode, load) -> {
                    final Map<LinkAttribute<?>, Double> reportLoad = nload.computeIfAbsent(srcNode,
                            k -> new HashMap<>());
                    load.forEach((attr, value) -> {
                        reportLoad.merge(attr, value, Double::sum);
                    });
                });
            });

            allNetworkDemand = ImmutableUtils.makeImmutableMap2(nload);
        }

        return allNetworkDemand;
    }

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

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> computeLoad = null;

    /**
     * Get compute load for this node. This is a measured value. service -> node
     * load is coming from -> {@link NodeAttribute} specifying the thing being
     * measured -> value.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getComputeLoad() {
        if (null == computeLoad) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<NodeIdentifier, Map<NodeAttribute<?>, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>> cload = report
                        .getComputeLoad();
                final ServiceIdentifier<?> service = report.getService();
                if (null != service) {
                    final Map<NodeIdentifier, Map<NodeAttribute<?>, Double>> serviceLoad = sload
                            .computeIfAbsent(service, k -> new HashMap<>());

                    cload.forEach((srcNode, values) -> {
                        final Map<NodeAttribute<?>, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcNode,
                                k -> new HashMap<>());
                        values.forEach((attr, value) -> {
                            sRegionLoad.merge(attr, value, Double::sum);
                        });
                    });
                }
            });

            computeLoad = ImmutableUtils.makeImmutableMap3(sload);
        }
        return computeLoad;
    }

    private transient ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> computeDemand = null;

    /**
     * Get estimated compute demand for this node. The meanings of the keys and
     * values match those from {@link #getComputeLoad()}, except that this is
     * referring to estimated demand rather than measured load.
     * 
     * @return the demand information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getComputeDemand() {
        if (null == computeDemand) {
            // compute it
            final Map<ServiceIdentifier<?>, Map<NodeIdentifier, Map<NodeAttribute<?>, Double>>> sload = new HashMap<>();
            containerReports.forEach((container, report) -> {
                final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>> cload = report
                        .getComputeDemand();
                final ServiceIdentifier<?> service = report.getService();
                if (null != service) {
                    final Map<NodeIdentifier, Map<NodeAttribute<?>, Double>> serviceLoad = sload
                            .computeIfAbsent(service, k -> new HashMap<>());

                    cload.forEach((srcNode, values) -> {
                        final Map<NodeAttribute<?>, Double> sRegionLoad = serviceLoad.computeIfAbsent(srcNode,
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

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> containerNetworkLoad = null;

    /**
     * Network load to neighboring nodes summed across the containers. See
     * {@link #getAllocatedNetworkCapacity()} for details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getContainerNetworkLoad() {
        if (null == containerNetworkLoad) {
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

            containerNetworkLoad = ImmutableUtils.makeImmutableMap2(nload);
        }
        return containerNetworkLoad;
    }

    private transient ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> containerNetworkDemand = null;

    /**
     * Network demand to neighboring nodes summed across the containers. See
     * {@link #getAllocatedNetworkCapacity()} for details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getContainerNetworkDemand() {
        if (null == containerNetworkDemand) {
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

            containerNetworkDemand = ImmutableUtils.makeImmutableMap2(nload);
        }
        return containerNetworkDemand;
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

                ImmutableMap.of(), // nodeNetworkCapacity
                ImmutableMap.of(), // nodeNetworkLoad
                ImmutableMap.of(), // nodeNetworkDemand

                ImmutableMap.of(), // nodeNeighborNetworkCapacity
                ImmutableMap.of(), // nodeNeighborNetworkLoad
                ImmutableMap.of(), // nodeNeighborNetworkDemand

                ImmutableMap.of()); // container reports
    }

    @Override
    public String toString() {
        return "{" + " node: " + getNodeName() + " nodeComputeCapacity: " + getNodeComputeCapacity()
                + " allocatedComputeCapacity: " + getAllocatedComputeCapacity() + " computeLoad: " + getComputeLoad()
                + " containerReports: " + getContainerReports() + "}";
    }
}
