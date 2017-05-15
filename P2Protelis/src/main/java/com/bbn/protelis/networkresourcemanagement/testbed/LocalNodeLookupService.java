package com.bbn.protelis.networkresourcemanagement.testbed;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.Node;
import com.bbn.protelis.networkresourcemanagement.NodeLookupService;

/**
 * Assume all nodes are running on localhost. A single instance must be used for
 * all lookups. Each node gets a port number starting at the base port and
 * counting up for each new node that is passed to
 * {@link #getInetAddressForNode(Node)}. This class is thread-safe.
 * 
 */
public class LocalNodeLookupService implements NodeLookupService {

	private final int basePort;
	private int nextAvailablePort;
	private final Object lock = new Object();
	private final Map<DeviceUID, InetSocketAddress> mapping = new HashMap<>();

	/**
	 * The base port to use for all Node connections.
	 * 
	 * @param basePort
	 *            a valid network port.
	 */
	public LocalNodeLookupService(final int basePort) {
		if (basePort <= 0 || basePort > 65535) {
			throw new IllegalArgumentException("Port must be between 0 and 65535");
		}
		this.basePort = basePort;
		nextAvailablePort = this.basePort;
	}

	@Override
	public InetSocketAddress getInetAddressForNode(final DeviceUID uid) {
		synchronized (lock) {
			if (mapping.containsKey(uid)) {
				return mapping.get(uid);
			} else {
				final int port = nextAvailablePort;
				final InetSocketAddress addr = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
				mapping.put(uid, addr);

				++nextAvailablePort;

				return addr;
			}
		}
	}

}
