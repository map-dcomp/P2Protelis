package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Information about a node at a particular point in time.
 * 
 * Capacity is a measured value that state how much of a particular resource a
 * server or network link has. The units are specified by the
 * {@link NodeAttribute} or {@link LinkAttribute} that is associted with the
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
     * @param serverLoad
     *            see {@link #getServerLoad()}
     * @param serverCapacity
     *            see {@link #getServerCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     * @param networkLoad
     *            see {@link #getNetworkLoad()}
     * @param demandEstimationWindow
     *            see {#link {@link #getDemandEstimationWindow()}
     * @param serverDemand
     *            see {@link #getServerDemand()}
     * @param networkDemand
     *            see {@link #getNetworkDemand()}
     */
    public ResourceReport(@Nonnull final NodeIdentifier nodeName,
            final long timestamp,
            @Nonnull final EstimationWindow demandEstimationWindow,
            @Nonnull final ImmutableMap<NodeAttribute<?>, Double> serverCapacity,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.serverLoad = serverLoad;
        this.serverCapacity = serverCapacity;
        this.serverDemand = serverDemand;
        this.networkCapacity = networkCapacity;
        this.networkLoad = networkLoad;
        this.networkDemand = networkDemand;

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

    private final EstimationWindow demandEstimationWindow;

    /**
     * @return the window over which the demand values are computed
     * @see #getNetworkDemand()
     * @see #getServerDemand()
     */
    @Nonnull
    public EstimationWindow getDemandEstimationWindow() {
        return demandEstimationWindow;
    }

    private final NodeIdentifier nodeName;

    /**
     * @return the identifier of the node that the report came from
     */
    public final NodeIdentifier getNodeName() {
        return nodeName;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad;

    /**
     * Get server load for this node. This is a measured value. server -> region
     * load is coming from -> {@link NodeAttribute} specifying the thing being
     * measured -> value.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getServerLoad() {
        return serverLoad;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand;

    /**
     * Get estimated server demand for this node. The meanings of the keys and
     * values match those from {@link #getServerLoad()}, except that this is
     * referring to estimated demand rather than measured load.
     * 
     * @return the demand information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getServerDemand() {
        return serverDemand;
    }

    private final ImmutableMap<NodeAttribute<?>, Double> serverCapacity;

    /**
     * Server capacity for each attribute of a node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity;

    /**
     * Link capacity for neighboring nodes. neighbor node -> attribute -> value.
     * Each key in the list is the identifier of a neighboring node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkCapacity() {
        return networkCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad;

    /**
     * Network load to neighboring nodes. See {@link #getNetworkCapacity()} for
     * details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkLoad() {
        return networkLoad;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand;

    /**
     * Network demand to neighboring nodes. See {@link #getNetworkCapacity()}
     * for details on the map definition.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkDemand() {
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
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = ImmutableMap
                .of();
        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap.of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad = ImmutableMap.of();

        return new ResourceReport(nodeName, NULL_TIMESTAMP, demandWindow, serverCapacity, serverLoad, serverLoad,
                networkCapacity, networkLoad, networkLoad);
    }

}
