package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.SimpleExecutionEnvironment;

import com.bbn.protelis.utils.StringUID;

/**
 * A node in the network.
 */
public class Node extends AbstractExecutionContext {

	/** Device numerical identifier */
	private final StringUID uid;

	/** The Protelis VM to be executed by the device */
	private final ProtelisVM vm;

	/**
	 * The neighboring nodes.
	 */
	private final Set<DeviceUID> neighbors = new HashSet<>();

	/**
	 * The neighbors of this {@link Node}.
	 * 
	 * @return unmodifiable set
	 */
	public Set<DeviceUID> getNeighbors() {
		return Collections.unmodifiableSet(neighbors);
	}

	/**
	 * Add a neighbor.
	 * 
	 * @param v
	 *            the UID of the neighbor
	 */
	public void addNeighbor(final DeviceUID v) {
		neighbors.add(v);
	}
	public void addNeighbor(final Node v) {
		addNeighbor(v.getDeviceUID());
	}

	/**
	 * @param program
	 *            the program to run on the node
	 * @param name
	 *            the name of the node (must be unique)
	 */
	public Node(final ProtelisProgram program, final String name) {
		super(new SimpleExecutionEnvironment(), new NodeNetworkManager());
		this.uid = new StringUID(name);

		// Finish making the new device and add it to our collection
		vm = new ProtelisVM(program, this);
	}

	/**
	 * Internal-only lightweight constructor to support "instance"
	 */
	private Node(final StringUID uid) {
		super(new SimpleExecutionEnvironment(), new NodeNetworkManager());
		this.uid = uid;
		vm = null;
	}

	/**
	 * Accessor for virtual machine, to allow external execution triggering
	 */
	public ProtelisVM getVM() {
		return vm;
	}

	/**
	 * Expose the network manager, to allow external simulation of network For
	 * real devices, the NetworkManager usually runs autonomously in its own
	 * thread(s)
	 */
	public NodeNetworkManager accessNetworkManager() {
		return (NodeNetworkManager) super.getNetworkManager();
	}

	public String getName() {
		return uid.getUID();
	}

	@Override
	public DeviceUID getDeviceUID() {
		return uid;
	}

	@Override
	public Number getCurrentTime() {
		return System.currentTimeMillis();
	}

	@Override
	protected AbstractExecutionContext instance() {
		return new Node(uid);
	}

	@Override
	public double nextRandomDouble() {
		return Math.random();
	}
}
