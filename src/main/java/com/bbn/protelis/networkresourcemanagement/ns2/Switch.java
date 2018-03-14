package com.bbn.protelis.networkresourcemanagement.ns2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A network switch in the topology. May also be referred to as a LAN.
 * 
 * @author jschewe
 *
 */
public class Switch extends NetworkDevice {

    /**
     * Create a switch and all implicit links.
     * 
     * @param name
     *            see {@link NetworkDevice#getName()}
     * @param nodes
     *            see {@link #getNodes()}
     * @param bandwidth
     *            used as the bandwidth for the created links see
     *            {@link Link#getBandwidth()}
     * @see Link#Link(String, NetworkDevice, NetworkDevice, double)
     */
    public Switch(@Nonnull final String name, @Nonnull final Set<Node> nodes, final double bandwidth) {
        super(name);

        nodes.forEach(n -> {
            final Link link = new Link(name, this, n, bandwidth);
            links.put(n, link);
        });

    }

    private final Map<Node, Link> links = new HashMap<>();

    /**
     * 
     * @return the links between this switch and other nodes. Unmodifiable
     *         object.
     */
    public Map<Node, Link> getLinks() {
        return Collections.unmodifiableMap(links);
    }

    /**
     * 
     * @return the nodes connected to the switch. Unmodifiable object.
     */
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(links.keySet());
    }

}
