package com.bbn.protelis.networkresourcemanagement.ns2;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * An NS2 network toplogy.
 * 
 * @author jschewe
 *
 */
public class Topology {

    /**
     * 
     * @param name
     *            see {@link #getName()}
     * @param nodes
     *            see {@link #getNodes()}
     */
    public Topology(@Nonnull final String name, @Nonnull final ImmutableMap<String, Node> nodes) {
        this.name = name;
        this.nodes = nodes;
    }

    private final String name;

    /**
     * 
     * @return name of the topology
     */
    @Nonnull
    public String getName() {
        return name;
    }

    private final ImmutableMap<String, Node> nodes;

    /**
     * 
     * @return the nodes in the topology, key is the node name
     */
    public ImmutableMap<String, Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Topology: " + getName();
    }
}
