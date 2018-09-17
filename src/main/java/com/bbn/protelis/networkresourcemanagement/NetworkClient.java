package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of clients of for services in the network. Clients are an
 * endpoint in a network topology. Clients can only connect to nodes, they
 * cannot connect to other clients.
 */
public class NetworkClient implements NetworkNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkClient.class);

    /**
     * The key into extra data passed to {@link #processExtraData(Map)} that
     * specifies the number of clients that this object represents.
     */
    public static final String EXTRA_DATA_NUM_CLIENTS_KEY = "numClients";

    /**
     * Create a client with the specified name.
     * 
     * @param id
     *            the name of the client
     * @param extraData
     *            defines values for extra properties
     */
    public NetworkClient(@Nonnull final NodeIdentifier id, @Nonnull final Map<String, Object> extraData) {
        this.uid = id;

        final String regionName = NetworkServerProperties.parseRegionName(extraData);
        if (null != regionName) {
            final StringRegionIdentifier region = new StringRegionIdentifier(regionName);
            this.region = region;
        } else {
            this.region = StringRegionIdentifier.UNKNOWN;
        }

        final Object numClientsValue = extraData.get(EXTRA_DATA_NUM_CLIENTS_KEY);
        if (null != numClientsValue) {
            try {
                final int numClients = Integer.parseInt(numClientsValue.toString());
                setNumClients(numClients);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Unable to parse {} as an integer: {}, not setting number of clients", numClientsValue,
                        e.getMessage());
            }
        }

    }

    private final NodeIdentifier uid;

    @Override
    @Nonnull
    public final NodeIdentifier getNodeIdentifier() {
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

    private int numClients = 1;

    /**
     * @return the number of clients that this object represents, defaults to 1.
     */
    public int getNumClients() {
        return numClients;
    }

    private void setNumClients(final int v) {
        numClients = v;
    }

    private final RegionIdentifier region;

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
