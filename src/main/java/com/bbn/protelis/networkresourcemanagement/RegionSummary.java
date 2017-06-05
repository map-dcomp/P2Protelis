package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Summary information about the resources in a region.
 * 
 */
public class RegionSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    /**
     * 
     * @param name
     *            the name of this region
     * @param clientDemand
     *            the client demand for services from this region. Key is
     *            service name, value is map of attribute to value
     * @param serverCapacity
     *            the capacity of the servers in this region
     * @param neighborLinkCapacity
     *            the capacity of the links to the neighboring regions (key is
     *            region name)
     * @param neighborLinkDemand
     *            the demand on the links to the neighboring regions (key is
     *            region name)
     */
    public RegionSummary(@Nonnull final String name,
            @Nonnull final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.name = name;
        this.clientDemand = clientDemand;
        this.serverCapacity = serverCapacity;
        this.neighborLinkCapacity = neighborLinkCapacity;
        this.neighborLinkDemand = neighborLinkDemand;
    }

    /**
     * @return the name of the region
     */
    public final String getRegionName() {
        return name;
    }

    private final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand;

    /**
     * Get client demand for this region. Key is the service name, value is the
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
     * Server capacity for this region.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity;

    /**
     * Link capacity for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        return neighborLinkCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Link demand for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

    /**
     * 
     * @param name
     *            the name of the region
     * @return empty summary for a region
     */
    public static RegionSummary getNullSummary(@Nonnull final String name) {
        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand = ImmutableMap.of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap.of();

        return new RegionSummary(name, clientDemand, serverCapacity, neighborLinkCapacity, neighborLinkDemand);
    }

}
