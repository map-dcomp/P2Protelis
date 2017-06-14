package com.bbn.protelis.networkresourcemanagement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network manager for a {@link NetworkServer}.
 */
public class NodeNetworkManager implements NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeNetworkManager.class);

    private static final Random RANDOM = new Random();

    private final NodeLookupService lookupService;
    private final Object lock = new Object();

    private NetworkServer node;

    /**
     * 
     * @param lookupService
     *            how to find other nodes
     */
    public NodeNetworkManager(final NodeLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        final Map<DeviceUID, Map<CodePath, Object>> retval = new HashMap<>();

        synchronized (lock) {
            for (final Map.Entry<DeviceUID, NetworkNeighbor> entry : nbrs.entrySet()) {
                retval.put(entry.getKey(), entry.getValue().getSharedValues());
            }
        }

        return retval;
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        // copy the list so that we don't hold the lock while sending all of the
        // messages
        final Map<DeviceUID, NetworkNeighbor> nbrsCopy = new HashMap<>();
        synchronized (lock) {
            nbrsCopy.putAll(nbrs);
        }

        final Map<DeviceUID, NetworkNeighbor> toRemove = new HashMap<>();
        for (final Map.Entry<DeviceUID, NetworkNeighbor> entry : nbrsCopy.entrySet()) {
            try {
                entry.getValue().sendMessage(toSend);
            } catch (final IOException e) {
                LOGGER.error("Got error sending message to neighbor, removing. Neighbor: " + entry.getKey(), e);
                toRemove.put(entry.getKey(), entry.getValue());
            }
        }

        if (!toRemove.isEmpty()) {
            synchronized (lock) {
                for (final Map.Entry<DeviceUID, NetworkNeighbor> entry : toRemove.entrySet()) {
                    entry.getValue().terminate();
                    nbrs.remove(entry.getKey());
                }
            }
        }
    }

    /**
     * Start communicating with neighbors.
     * 
     * @param node
     *            The node that this network manager exists for.
     * @throws IllegalStateException
     *             if the network manager is already running
     */
    public void start(final NetworkServer node) {
        synchronized (lock) {
            if (null != threadGroup) {
                throw new IllegalStateException(
                        "Cannot start network manager when it's already running. Node: " + node.getName());
            }

            threadGroup = new ThreadGroup("clients for " + node.getName());
            this.node = node;
            listenForNeighbors();

            for (final DeviceUID neighborUID : node.getNeighbors()) {
                connectToNeighbor(neighborUID);
            }
        }
    }

    private ThreadGroup threadGroup = null;

    /**
     * Stop the manager.
     */
    public void stop() {
        synchronized (lock) {
            if (null != threadGroup) {
                threadGroup.interrupt();
                final int numThreads = threadGroup.activeCount();
                final Thread[] activeThreads = new Thread[numThreads];
                final int numActiveThreads = threadGroup.enumerate(activeThreads);
                for (int i = 0; i < numActiveThreads; ++i) {
                    try {
                        activeThreads[i].join();
                    } catch (final InterruptedException e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Interrupted waiting for thread join", e);
                        }
                    }
                }
            }
        }
    }

    private final Map<DeviceUID, NetworkNeighbor> nbrs = new HashMap<>();

    private void addNeighbor(final DeviceUID uid,
            final int nonce,
            final Socket s,
            final ObjectInputStream in,
            final ObjectOutputStream out) {
        synchronized (lock) {
            // symmetry-break nonce
            // If UID isn't already linked, add a new neighbor

            final InetSocketAddress remoteAddr = new InetSocketAddress(s.getInetAddress(), s.getPort());
            final NetworkNeighbor other = nbrs.get(uid);
            if (null == other || other.getNonce() < nonce) {
                if (null != other) {
                    other.terminate();
                }

                final NetworkNeighbor neighbor = new NetworkNeighbor(this.threadGroup, uid, nonce, remoteAddr, s, in,
                        out);
                nbrs.put(uid, neighbor);
                neighbor.start();
            } else {
                try {
                    s.close();
                } catch (final IOException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Error closing parallel neighbor socket, ignoring", e);
                    }
                }
            }
        } // lock so that we don't add 2 connections to the neighbor
    }

    /**
     * Listen for neighbor connections.
     * 
     */
    private void listenForNeighbors() {

        final InetSocketAddress addr = lookupService.getInetAddressForNode(node.getDeviceUID());
        final int port = addr.getPort();
        new Thread(threadGroup, () -> {

            while (!Thread.interrupted()) {
                try (ServerSocket server = new ServerSocket(port)) {
                    server.setReuseAddress(true);
                    LOGGER.info("Node: " + node.getName() + " Daemon listening for neighbors on port " + port);
                    while (!Thread.interrupted()) {
                        final Socket s = server.accept();
                        final String threadName = "Node: " + node.getName() + " client: "
                                + s.getRemoteSocketAddress().toString();
                        new Thread(this.threadGroup, () -> {
                            try {
                                // If the link connects, trade UIDs
                                final ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                                final ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                                // write uid for neighbor
                                out.writeObject(node.getDeviceUID());
                                out.flush();

                                // reads data from connectToNeighbor()
                                final DeviceUID uid = (DeviceUID) in.readObject();
                                final int nonce = in.readInt();

                                addNeighbor(uid, nonce, s, in, out);

                                // Given that all else is successful, note link
                                // in
                                // link table
                                // linkToNbr.put(remoteAddr, uid);
                            } catch (final IOException | ClassNotFoundException e) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Got exception creating link to neighbor that connected to us.", e);
                                }
                            }
                        }, threadName).start();
                    } // while !interrupted accept connections
                } catch (final IOException e) {
                    LOGGER.warn("Node: " + node.getName()
                            + " received I/O exception accepting connections, trying to listen again", e);
                }
            } // while !interrupted, restart listen

        }, "Node: " + node.getName() + " server thread").start();

    }

    /**
     * Attempt to create a connection to a neighbor.
     * 
     * @param neighborNode
     *            the neighbor to connect to
     */
    private void connectToNeighbor(final DeviceUID neighborUID) {
        final InetSocketAddress addr = lookupService.getInetAddressForNode(neighborUID);
        if (null == addr) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(neighborUID + " is not found in the lookup service, assuming this is a client");
            }
            return;
        }

        try {
            // Try to link
            final Socket s = new Socket(addr.getAddress(), addr.getPort());

            final int nonce = RANDOM.nextInt();

            // If the link connects, trade UIDs
            final ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            final ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            final DeviceUID neighborUid = (DeviceUID) in.readObject();

            out.writeObject(node.getDeviceUID());
            out.writeInt(nonce);
            out.flush();

            addNeighbor(neighborUid, nonce, s, in, out);
        } catch (final IOException | ClassNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Couldn't connect to neighbor: " + neighborUID, e);
            }
        }
    }

}
