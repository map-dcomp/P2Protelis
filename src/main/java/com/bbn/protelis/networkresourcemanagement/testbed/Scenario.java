package com.bbn.protelis.networkresourcemanagement.testbed;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * A test scenario.
 * 
 * @author jschewe
 */
public class Scenario {
	private boolean visualize = true;

	/**
	 * Should the system run with visualization, or headless?
	 */
	public void setVisualize(final boolean v) {
		this.visualize = v;
	}

	public boolean getVisualize() {
		return this.visualize;
	}

	/**
	 * Name of scenario, to put into log files and visualization window name.
	 */
	private final String name;

	public String getName() {
		return name;
	}

	/**
	 * Nodes in the network to run, including Protelis program for each device.
	 */
	public Map<DeviceUID, Node> getNodes() {
		return Collections.unmodifiableMap(this.nodes);
	}

	private final Map<DeviceUID, Node> nodes = new HashMap<>();

	/**
	 * Links in the network.
	 */
	public Set<Link> getLinks() {
		return Collections.unmodifiableSet(this.links);
	}

	private final Set<Link> links = new HashSet<>();

	/**
	 * Constructor for creating a scenario with default conditions.
	 */
	public Scenario(final String name, final Map<DeviceUID, Node> nodes, final Set<Link> links) {
		this.name = name;
		this.nodes.putAll(nodes);
		this.links.addAll(links);
	}

}
