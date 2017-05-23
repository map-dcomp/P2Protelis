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

    private static final int MIN_NETWORK_PORT = 0;
    private static final int MAX_NETWORK_PORT = 65535;

    /**
     * The base port to use for all Node connections.
     * 
     * @param basePort
     *            a valid network port.
     */
    public LocalNodeLookupService(final int basePort) {
        if (basePort <= MIN_NETWORK_PORT || basePort > MAX_NETWORK_PORT) {
            throw new IllegalArgumentException("Port must be between " + MIN_NETWORK_PORT + " and " + MAX_NETWORK_PORT);
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
