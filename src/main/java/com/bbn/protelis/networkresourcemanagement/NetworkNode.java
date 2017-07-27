package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Common functionality between {@link NetworkServer} and {@link NetworkClient}.
 * 
 */
public interface NetworkNode {

    /**
     * 
     * @return the ID of the object
     */
    @Nonnull
    NodeIdentifier getNodeIdentifier();

    /**
     * @return the name of the node
     */
    @Nonnull
    String getName();

    /**
     * @return the name of the region that this node currently belongs to, may
     *         be null
     */
    RegionIdentifier getRegionIdentifier();

    /**
     * Process the extra data that was found when creating the node.
     * 
     * @param extraData
     *            key/value pairs
     * @see NetworkFactory#createServer(String, java.util.Map)
     * @see NetworkFactory#createClient(String, Map)
     */
    void processExtraData(@Nonnull Map<String, Object> extraData);

    /**
     * Add a neighbor. If the neighbor node already exists, the bandwidth
     * capacity for the neighbor is replaced with the new value.
     * 
     * @param v
     *            the UID of the neighbor node
     * @param bandwidth
     *            capacity to the neighbor in bytes per second. Infinity can be
     *            used for unknown.
     */
    void addNeighbor(@Nonnull NodeIdentifier v, double bandwidth);

    /**
     * 
     * @param v
     *            the neighbor to add
     * @param bandwidth
     *            to the neighbor in bytes per second
     * @see #addNeighbor(NodeIdentifier, double)
     */
    void addNeighbor(@Nonnull NetworkNode v, double bandwidth);

    /**
     * The neighbors of this {@link NetworkServer}. Note that these IDs may
     * refer to either {@link NetworkServer} or {@link NetworkClient}.
     * 
     * @return unmodifiable set
     */
    @Nonnull
    Set<NodeIdentifier> getNeighbors();

    /**
     * 
     * @return the hardware platform for this node, may be null
     */
    String getHardware();

    /**
     * 
     * @param hardware
     *            the hardware platform for this node
     * @see #getHardware()
     */
    void setHardware(String hardware);

}
