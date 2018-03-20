package com.bbn.protelis.networkresourcemanagement.ns2;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Node in the NS2 topology.
 * 
 * @author jschewe
 *
 */
public class Node extends NetworkDevice {

    /**
     * Extra data key to specify if a client should be created or a node. If
     * specified and set to true, then a client is created.
     */
    public static final String EXTRA_DATA_CLIENT = "client";

    /**
     * 
     * @param name
     *            see {@link #getName()}
     * @param extraData
     *            see {@link #getExtraData()}
     */
    public Node(@Nonnull final String name, @Nonnull final Map<String, Object> extraData) {
        super(name);
        this.extraData = extraData;
    }

    private final Map<String, Object> extraData;

    /**
     * 
     * @return the data read from the nodes JSON file. This is the underlying
     *         object, it should not be modified.
     */
    @Nonnull
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    private final Map<Link, InetAddress> ipAddresses = new HashMap<>();

    /**
     * Specify an IP address for a link. This will overwrite any previous
     * address for this link on this node.
     * 
     * @param link
     *            the link
     * @param ip
     *            the address for the link
     */
    public void setIpAddress(@Nonnull final Link link, @Nonnull final InetAddress ip) {
        ipAddresses.put(link, ip);
    }

    /**
     * 
     * @param link
     *            the link to get the address for
     * @return the address or null if there is no address specified for the link
     */
    public InetAddress getIpAddress(@Nonnull final Link link) {
        return ipAddresses.get(link);
    }

    /**
     * @return unmodifiable map of link -> address, note that an address may be
     *         null
     */
    public Map<Link, InetAddress> getAllIpAddresses() {
        return Collections.unmodifiableMap(ipAddresses);
    }

    /**
     * Add to the list of known links. This initializes the link with an IP
     * address of null.
     * 
     * @param link
     *            the link to add
     */
    public void addLink(@Nonnull final Link link) {
        if (ipAddresses.containsKey(link)) {
            throw new IllegalArgumentException("Node already contains the link: " + link);
        }

        ipAddresses.put(link, null);
    }

    /**
     * 
     * @return the links attached to this node. Unmodifiable object.
     */
    public Set<Link> getLinks() {
        return Collections.unmodifiableSet(ipAddresses.keySet());
    }

    private String operatingSystem = null;

    /**
     * 
     * @return operating system running on the node, defaults to null
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * 
     * @param v
     *            the new operating system value
     * @see #getOperatingSystem()
     */
    public void setOperatingSystem(final String v) {
        operatingSystem = v;

    }

    private String hardware = null;

    /**
     * 
     * @return the hardware specified in the topology, defaults to null
     */
    public String getHardware() {
        return hardware;
    }

    /**
     * 
     * @param v
     *            the new hardware
     * @see #getHardware()
     */
    public void setHardware(final String v) {
        hardware = v;
    }

    /**
     * 
     * @return true if this node is a client
     */
    public boolean isClient() {
        final Object client = extraData.get(EXTRA_DATA_CLIENT);
        if (null != client) {
            return Boolean.parseBoolean(client.toString());
        } else {
            return false;
        }
    }

}
