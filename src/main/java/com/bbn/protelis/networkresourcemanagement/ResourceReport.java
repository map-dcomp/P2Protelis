package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Information about a node at a particular point in time.
 */
public class ResourceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param nodeName
     *            the name of the node that this report is from
     * @param timestamp
     *            the time that the ResourceReport was generated.
     * @param clientDemand
     *            the demand from the clients of the service
     * @param serverCapacity
     *            the server capacity for this service
     * @param neighborLinkCapacity
     *            the network capacity to neighbors for this service
     * @param neighborLinkDemand
     *            the network demand to neighbors for this service
     */
    public ResourceReport(@Nonnull final NodeIdentifier nodeName,
            final long timestamp,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeAttribute, Double>> clientDemand,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity,
            @Nonnull final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.clientDemand = clientDemand;
        this.serverCapacity = serverCapacity;
        this.neighborLinkCapacity = neighborLinkCapacity;
        this.neighborLinkDemand = neighborLinkDemand;

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

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeAttribute, Double>> clientDemand;

    /**
     * Get client demand for this node. Key is the service name, value is the
     * demand by {@link NodeAttribute}.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeAttribute, Double>> getClientDemand() {
        return clientDemand;
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

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity;

    /**
     * Link capacity for neighboring nodes. Key is node name.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        return neighborLinkCapacity;
    }

    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Link demand for neighboring nodes. Key is node name.
     * 
     * @return Not null.
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

    /**
     * Create a resource report with no data. The timestamp is set to -1.
     * 
     * @param nodeName
     *            the name of the node
     * @return empty report for a node
     */
    public static ResourceReport getNullReport(@Nonnull final NodeIdentifier nodeName) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeAttribute, Double>> clientDemand = ImmutableMap.of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = ImmutableMap
                .of();
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap.of();

        return new ResourceReport(nodeName, -1, clientDemand, serverCapacity, neighborLinkCapacity, neighborLinkDemand);
    }

}
