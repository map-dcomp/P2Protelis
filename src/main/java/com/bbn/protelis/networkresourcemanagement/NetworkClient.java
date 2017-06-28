package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Represents a set of clients of for services in the network. Clients are an
 * endpoint in a network topology. Clients can only connect to nodes, they
 * cannot connect to other clients.
 */
public class NetworkClient implements NetworkNode {

    /**
     * Create a client with the specified name.
     * 
     * @param name
     *            the name of the client.
     */
    public NetworkClient(@Nonnull final String name) {
        this.uid = new StringNodeIdentifier(name);
        this.region = NetworkServer.NULL_REGION;
    }

    private final StringNodeIdentifier uid;

    @Override
    @Nonnull
    public final StringNodeIdentifier getNodeIdentifier() {
        return uid;
    }

    /**
     * The neighboring nodes.
     */
    private final Map<NodeIdentifier, Double> neighborNodes = new HashMap<>();

    @Override
    @Nonnull
    public final Set<NodeIdentifier> getNeighbors() {
        return Collections.unmodifiableSet(neighborNodes.keySet());
    }

    @Override
    public final void addNeighbor(@Nonnull final NodeIdentifier v, final double bandwidth) {
        neighborNodes.put(v, bandwidth);
    }

    @Override
    public final void addNeighbor(@Nonnull final NetworkNode v, final double bandwidth) {
        addNeighbor(v.getNodeIdentifier(), bandwidth);
    }

    @Override
    public void processExtraData(@Nonnull final Map<String, Object> extraData) {
        final Object regionValue = extraData.get(NetworkServer.EXTRA_DATA_REGION_KEY);
        if (null != regionValue) {
            final String regionName = regionValue.toString();
            final StringRegionIdentifier region = new StringRegionIdentifier(regionName);
            this.setRegion(region);
        }
    }

    private RegionIdentifier region;

    private void setRegion(final RegionIdentifier region) {
        this.region = region;
    }

    @Override
    public RegionIdentifier getRegionIdentifier() {
        return this.region;
    }

    private String hardware;

    @Override
    public String getHardware() {
        return hardware;
    }

    @Override
    public void setHardware(final String hardware) {
        this.hardware = hardware;
    }

    @Override
    public String getName() {
        return getNodeIdentifier().getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
