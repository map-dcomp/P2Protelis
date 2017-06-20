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
        this.regionName = NetworkServer.NULL_REGION_NAME;
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
        final Object region = extraData.get(NetworkServer.EXTRA_DATA_REGION_KEY);
        if (null != region) {
            this.setRegionName(region.toString());
        }
    }

    private String regionName;

    private void setRegionName(final String region) {
        this.regionName = region;
    }

    @Override
    public String getRegionName() {
        return this.regionName;
    }

}
