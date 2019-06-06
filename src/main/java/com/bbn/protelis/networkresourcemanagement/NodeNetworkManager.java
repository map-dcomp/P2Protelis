/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
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
import java.util.stream.Collectors;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network manager for a {@link NetworkServer}.
 */
public class NodeNetworkManager implements NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeNetworkManager.class);

    /* package */ static final Logger PROFILE_LOGGER = LoggerFactory
            .getLogger("com.bbn.protelis.networkresourcemanagement.profile");

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

    private static void unwrapAndLog(final int depth, final Object o) {
        if (o instanceof Tuple) {
            final Tuple t = (Tuple) o;
            for (final Object oo : t.toArray()) {
                unwrapAndLog(depth + 1, oo);
            }
        } else {
            final StringBuilder message = new StringBuilder();
            message.append("Sharing object of type: ");
            if (null == o) {
                message.append("NULL");
            } else {
                message.append(o.getClass().toString());
            }
            message.append(" depth: ");
            message.append(depth);

            if (o instanceof ResourceSummary) {
                final ResourceSummary rs = (ResourceSummary) o;
                message.append(" sc.size: " + rs.getServerCapacity().size());
                message.append(" sl.size: " + rs.getServerLoad().size());
                rs.getServerLoad().forEach((service, serviceMap) -> {
                    message.append(" sl.size.size: " + serviceMap.size());
                    serviceMap.forEach((region, regionMap) -> {
                        message.append(" sl.size.size.size: " + regionMap.size());
                    });
                });

                message.append(" sd.size: " + rs.getServerDemand().size());
                rs.getServerDemand().forEach((service, serviceMap) -> {
                    message.append(" sd.size.size: " + serviceMap.size());
                    serviceMap.forEach((region, regionMap) -> {
                        message.append(" sd.size.size.size: " + regionMap.size());
                    });
                });

                message.append(" nc.size: " + rs.getNetworkCapacity().size());
                rs.getNetworkCapacity().forEach((region, regionMap) -> {
                    message.append(" nc.size.size: " + regionMap.size());
                });
                message.append(" nl.size: " + rs.getNetworkLoad().size());
                rs.getNetworkLoad().forEach((region, regionMap) -> {
                    message.append(" nl.size.size: " + regionMap.size());
                });
                message.append(" nd.size: " + rs.getNetworkDemand().size());
                rs.getNetworkDemand().forEach((region, regionMap) -> {
                    message.append(" nd.size.size: " + regionMap.size());
                });

            } else if (o instanceof LoadBalancerPlan) {
                final LoadBalancerPlan p = (LoadBalancerPlan) o;
                message.append(" size: " + p.getServicePlan().size());
            } else if (o instanceof RegionPlan) {
                final RegionPlan p = (RegionPlan) o;
                message.append(" size: " + p.getPlan().size());
            }

            LOGGER.trace(message.toString());
        }
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        // copy the list so that we don't hold the lock while sending all of the
        // messages
        final Map<DeviceUID, NetworkNeighbor> nbrsCopy = new HashMap<>();
        synchronized (lock) {
            nbrsCopy.putAll(nbrs);
        }

        PROFILE_LOGGER.debug("Top of share AP round {} sending from {} - neighbors: {}", node.getExecutionCount(), node,
                nbrsCopy.keySet());

        if (LOGGER.isTraceEnabled()) {
            toSend.forEach((code, o) -> {
                unwrapAndLog(0, o);
            });
        }

        final Map<DeviceUID, NetworkNeighbor> toRemove = new HashMap<>();
        for (final Map.Entry<DeviceUID, NetworkNeighbor> entry : nbrsCopy.entrySet()) {
            final NetworkNeighbor neighbor = entry.getValue();
            try {
                neighbor.sendMessage(toSend);
            } catch (final IOException e) {
                if (!neighbor.isRunning()) {
                    LOGGER.debug("Neighbor has stopped, removing. Neighbor: " + entry.getKey(), e);
                } else {
                    LOGGER.error("Got error sending message to neighbor, removing. Neighbor: " + entry.getKey(), e);
                }
                toRemove.put(entry.getKey(), neighbor);
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

        PROFILE_LOGGER.debug("Bottom of share AP round {} sending from {}", node.getExecutionCount(), node);
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
            if (running) {
                throw new IllegalStateException(
                        "Cannot start network manager when it's already running. Node: " + node.getName());
            }

            running = true;

            this.node = node;
            listenForNeighbors();
        }
    }

    /**
     * Connect to all neighbors that aren't currently connected and remove any
     * neighbors that aren't running. This should be called at regular intervals
     * to ensure that all neighbors are connected.
     */
    public void updateNeighbors() {
        // copy the list so that we don't hold the lock while sending all of
        // the messages and to ensure we don't end up with a
        // concurrent modification exception below
        final Map<DeviceUID, NetworkNeighbor> nbrsCopy = new HashMap<>();
        synchronized (lock) {
            // remove any disconnected neighbors
            nbrs.entrySet().removeIf(e -> !e.getValue().isRunning());
            
            nbrsCopy.putAll(nbrs);
        }

        for (final DeviceUID neighborUID : node.getApNeighbors()) {
            if (!nbrsCopy.containsKey(neighborUID)) {
                connectToNeighbor(neighborUID);
            }
        }
    }

    /**
     * @return if this node has connected to all of it's neighbors for AP
     *         communication
     */
    public boolean isConnectedToAllNeighbors() {
        synchronized (lock) {
            if (!node.getApNeighbors().stream().allMatch(n -> nbrs.containsKey(n))) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Round {}: {} not connected: {}", node.getExecutionCount(), node.getName(), node
                            .getApNeighbors().stream().filter(n -> !nbrs.containsKey(n)).collect(Collectors.toList()));
                }
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean running = false;

    /**
     * Stop the manager.
     */
    public void stop() {
        synchronized (lock) {
            running = false;

            // stop talking to neighbors
            nbrs.forEach((k, v) -> {
                v.terminate();
                try {
                    v.join();
                } catch (final InterruptedException e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Interrupted during join", e);
                    }
                }
            });

            // force one to stop listening
            try {
                if (null != server) {
                    server.close();
                }
            } catch (final IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Error closing server socket", e);
                }
            }
        }
    }

    /** neighbor -> connection */
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
                    LOGGER.debug("Closing remote connection from {} because there's a connection to them", uid);
                    other.terminate();
                }

                final NetworkNeighbor neighbor = new NetworkNeighbor(node, uid, nonce, remoteAddr, s, in, out);
                nbrs.put(uid, neighbor);
                neighbor.start();
            } else {
                LOGGER.debug("Closing connection to {} because we already have a connection from them", uid,
                        node.getDeviceUID());
                try {
                    out.writeObject(NetworkNeighbor.CLOSE_CONNECTION);
                } catch (final IOException e) {
                    LOGGER.debug("Error writing close to neighbor, ignoring", e);
                }

                try {
                    out.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing output to neighbor, ignoring", e);
                }

                try {
                    in.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing input from neighbor, ignoring", e);
                }

                try {
                    s.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing socket to neighbor, ignoring", e);
                }
            }
        } // lock so that we don't add 2 connections to the neighbor
    }

    private ServerSocket server = null;

    /**
     * Listen for neighbor connections.
     * 
     */
    private void listenForNeighbors() {

        final InetSocketAddress addr = lookupService.getInetAddressForNode(node.getNodeIdentifier());
        if (null == addr) {
            LOGGER.error(
                    "Unable to find this node '{}' in the lookup service, unable to listen for neighbor connections",
                    node.getNodeIdentifier());
            return;
        }

        final int port = addr.getPort();
        new Thread(() -> {

            while (running) {
                try {
                    synchronized (lock) {
                        server = new ServerSocket(port);
                    }

                    server.setReuseAddress(true);
                    LOGGER.info("Node: " + node.getName() + " Daemon listening for neighbors on port " + port);
                    while (running) {
                        final Socket s = server.accept();

                        // don't need a thread here since addNeighbor will take
                        // care of creating a thread to service the connection.
                        try {
                            // If the link connects, trade UIDs
                            final ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                            final ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                            // write uid for neighbor
                            out.writeObject(node.getNodeIdentifier());
                            out.flush();

                            // reads data from connectToNeighbor()
                            final DeviceUID uid = (DeviceUID) in.readObject();
                            final int nonce = in.readInt();

                            addNeighbor(uid, nonce, s, in, out);
                        } catch (final IOException | ClassNotFoundException e) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Got exception creating link to neighbor that connected to us.", e);
                            }
                        }

                    } // while running accept connections
                } catch (final IOException e) {
                    if (running) {
                        LOGGER.warn("Node: " + node.getName()
                                + " received I/O exception accepting connections, trying to listen again on port: "
                                + port, e);
                    }
                }

                synchronized (lock) {
                    try {
                        if (null != server) {
                            server.close();
                        }
                    } catch (final IOException e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Error closing server socket", e);
                        }
                    }
                    server = null;
                }
            } // while running, restart listen

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Exiting thread: " + Thread.currentThread().getName());
            }
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
            LOGGER.warn(neighborUID
                    + " is not found in the lookup service, not connecting to this neighbor for AP sharing");
            return;
        }

        LOGGER.debug("Connecting to {} from {}", neighborUID, node.getDeviceUID());

        try {
            // Try to link
            final Socket s = new Socket(addr.getAddress(), addr.getPort());

            final int nonce = RANDOM.nextInt();

            // If the link connects, trade UIDs
            final ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            final ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            final DeviceUID neighborUid = (DeviceUID) in.readObject();

            out.writeObject(node.getNodeIdentifier());
            out.writeInt(nonce);
            out.flush();

            addNeighbor(neighborUid, nonce, s, in, out);
        } catch (final IOException | ClassNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Couldn't connect to neighbor: {}. Will try again later.", neighborUID, e);
            }
        }
    }

}
