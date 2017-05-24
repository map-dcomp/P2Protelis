package com.bbn.protelis.networkresourcemanagement.testbed;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * A test scenario.
 * 
 * @param <N>
 *            the node type
 * @param <L>
 *            the link type
 */
public class Scenario<N extends Node, L extends Link> {
    private boolean visualize = false;

    /**
     * Should the system run with visualization, or headless?
     * 
     * @param v
     *            true if the scenario should be displayed
     */
    public void setVisualize(final boolean v) {
        this.visualize = v;
    }

    /**
     * 
     * @return should the scenario be displayed?
     * @see #setVisualize(boolean)
     */
    public boolean getVisualize() {
        return this.visualize;
    }

    private TerminationCondition<Map<DeviceUID, N>> terminationCondition;

    /**
     * @return may be null
     */
    public final TerminationCondition<Map<DeviceUID, N>> getTerminationCondition() {
        return terminationCondition;
    }

    private long terminationPollFrequency = 1 * 1000;

    /**
     * @return number of milliseconds between checks for termination
     */
    public final long getTerminationPollFrequency() {
        return terminationPollFrequency;
    }

    /**
     * @param v
     *            milliseconds between checks for termination
     */
    public final void setTerminationPollFrequency(final long v) {
        terminationPollFrequency = v;
    }

    /**
     * Condition for exiting the scenario.
     * 
     * @param v
     *            the new value, may be null
     */
    public final void setTerminationCondition(final TerminationCondition<Map<DeviceUID, N>> v) {
        terminationCondition = v;
    }

    /**
     * Name of scenario, to put into log files and visualization window name.
     */
    private final String name;

    /**
     * @return name of the scenario
     */
    public String getName() {
        return name;
    }

    /**
     * Nodes in the network to run, including Protelis program for each device.
     * 
     * @return unmodifiable map of the nodes, key is the UID
     */
    public Map<DeviceUID, N> getNodes() {
        return Collections.unmodifiableMap(this.nodes);
    }

    private final Map<DeviceUID, N> nodes = new HashMap<>();

    /**
     * Links in the network.
     * 
     * @return unmodifiable list of the links
     */
    public Set<L> getLinks() {
        return Collections.unmodifiableSet(this.links);
    }

    private final Set<L> links = new HashSet<>();

    /**
     * Constructor for creating a scenario with default conditions.
     * 
     * @param name
     *            the name of the scenario
     * @param nodes
     *            the nodes in the scenario
     * @param links
     *            the links in the scenario
     */
    public Scenario(final String name, final Map<DeviceUID, N> nodes, final Set<L> links) {
        this.name = name;
        this.nodes.putAll(nodes);
        this.links.addAll(links);
    }

}
