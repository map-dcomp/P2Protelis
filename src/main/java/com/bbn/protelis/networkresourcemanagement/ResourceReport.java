package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Information about a node at a particular point in time.
 */
public class ResourceReport {

    /**
     * 
     * @param nodeName
     *            the name of the node that this report is from
     * @param clientDemand
     *            the demand from the clients of the service
     * @param serverCapacity
     *            the server capacity for this service
     * @param neighborLinkCapacity
     *            the network capacity to neighbors for this service
     * @param neighborLinkDemand
     *            the network demand to neighbors for this service
     */
    public ResourceReport(@Nonnull final String nodeName,
            @Nonnull final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.nodeName = nodeName;
        this.clientDemand = clientDemand;
        this.serverCapacity = serverCapacity;
        this.neighborLinkCapacity = neighborLinkCapacity;
        this.neighborLinkDemand = neighborLinkDemand;

    }

    private final String nodeName;

    /**
     * @return the name of the node that the report came from
     */
    public final String getNodeName() {
        return nodeName;
    }

    private final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand;

    /**
     * Get client demand for this node. Key is the service name, value is the
     * demand by {@link NodeAttribute}.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> getClientDemand() {
        return clientDemand;
    }

    private final ImmutableMap<NodeAttribute, Double> serverCapacity;

    /**
     * Server capacity for the service on the node.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity;

    /**
     * Link capacity for neighboring nodes. Key is node name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        return neighborLinkCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Link demand for neighboring nodes. Key is node name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

    /**
     * 
     * @param nodeName
     *            the name of the node
     * @return empty report for a node
     */
    public static ResourceReport getNullReport(@Nonnull final String nodeName) {
        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand = ImmutableMap.of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap.of();

        return new ResourceReport(nodeName, clientDemand, serverCapacity, neighborLinkCapacity, neighborLinkDemand);
    }

}
