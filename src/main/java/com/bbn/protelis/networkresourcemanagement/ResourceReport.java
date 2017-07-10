package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Information about a node at a particular point in time.
 */
public class ResourceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp for null reports.
     */
    public static final long NULL_TIMESTAMP = -1;

    /**
     * 
     * @param nodeName
     *            the name of the node that this report is from
     * @param timestamp
     *            the time that the ResourceReport was generated.
     * @param serverLoad
     *            the current load on this service and the region is load is
     *            coming from
     * @param serverCapacity
     *            the server capacity for this service
     * @param networkCapacity
     *            the network capacity to neighbors for this service
     * @param networkLoad
     *            the network load from neighbors for this service and the
     *            region the load is coming from
     */
    public ResourceReport(@Nonnull final NodeIdentifier nodeName,
            final long timestamp,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverLoad,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkLoad) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.serverLoad = serverLoad;
        this.serverCapacity = serverCapacity;
        this.networkCapacity = networkCapacity;
        this.networkLoad = networkLoad;

    }

    private final long timestamp;

    /**
     * The units of the timestamp are determinted by the clock used for the
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

    private final NodeIdentifier nodeName;

    /**
     * @return the identifier of the node that the report came from
     */
    public final NodeIdentifier getNodeName() {
        return nodeName;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverLoad;

    /**
     * Get server load for this node. Key is the service name, value is the load
     * of each {@link NodeAttribute} by region.
     * 
     * @return the load information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>>
            getServerLoad() {
        return serverLoad;
    }

    private final ImmutableMap<NodeAttribute, Double> serverCapacity;

    /**
     * Server capacity for the service on the node.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity;

    /**
     * Link capacity for neighboring nodes. Key is node name.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> getNetworkCapacity() {
        return networkCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkLoad;

    /**
     * Load neighboring nodes. Key is node name.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> getNetworkLoad() {
        return networkLoad;
    }

    /**
     * Create a resource report with no data. The timestamp is set to
     * {@link #NULL_TIMESTAMP}.
     * 
     * @param nodeName
     *            the name of the node
     * @return empty report for a node
     */
    public static ResourceReport getNullReport(@Nonnull final NodeIdentifier nodeName) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute, Double>>> serverLoad = ImmutableMap
                .of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkCapacity = ImmutableMap.of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> networkLoad = ImmutableMap.of();

        return new ResourceReport(nodeName, NULL_TIMESTAMP, serverCapacity, serverLoad, networkCapacity, networkLoad);
    }

}
