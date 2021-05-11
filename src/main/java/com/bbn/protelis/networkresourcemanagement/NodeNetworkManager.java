/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTObjectOutput;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.CodePath;
import org.protelis.vm.NetworkManager;
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
            for (final Map.Entry<NodeIdentifier, NetworkNeighbor> entry : nbrs.entrySet()) {
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

            if (o instanceof ResourceReport) {
                final ResourceReport rr = (ResourceReport) o;
                message.append("  window: " + rr.getDemandEstimationWindow());
                message.append("  nc.size: " + rr.getNetworkCapacity().size());
                message.append("  nd.size: " + rr.getNetworkDemand().size());
                message.append("  nl.size: " + rr.getNetworkLoad().size());
            } else if (o instanceof ResourceSummary) {
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

            PROFILE_LOGGER.trace(message.toString());
        }

    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        // make a copy because the data structure inside Protelis being passed
        // is actually LinkedHashMap and reading from that data structure can
        // alter
        // the internal structure of the map causing problems with serialization
        final Map<CodePath, Object> localSend = Collections.unmodifiableMap(new HashMap<>(toSend));

        // copy the list so that we don't hold the lock while sending all of the
        // messages
        final Map<DeviceUID, NetworkNeighbor> nbrsCopy = new HashMap<>();
        synchronized (lock) {
            nbrsCopy.putAll(nbrs);
        }

        LOGGER.debug("Top of share AP round {} sending from {} - neighbors: {}", node.getExecutionCount(), node,
                nbrsCopy.keySet());

        if (PROFILE_LOGGER.isDebugEnabled()) {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                try (FSTObjectOutput oos = new FSTObjectOutput(bytes)) {
                    oos.writeObject(localSend);
                } catch (final IOException e) {
                    PROFILE_LOGGER.error("Error writing object to byte stream for measurement", e);
                }
                final int size = bytes.size();
                PROFILE_LOGGER.debug("{} sharing state of {} bytes", node.getNodeIdentifier(), size);
            } catch (final IOException e) {
                NodeNetworkManager.PROFILE_LOGGER.error("Error constructing byte stream for measurement", e);
            }
        }

        if (PROFILE_LOGGER.isTraceEnabled()) {
            localSend.forEach((code, o) -> {
                if (PROFILE_LOGGER.isDebugEnabled()) {
                    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                        try (FSTObjectOutput oos = new FSTObjectOutput(bytes)) {
                            oos.writeObject(o);
                        } catch (final IOException e) {
                            PROFILE_LOGGER.error("Error writing object to send to byte stream for measurement", e);
                        }
                        final int size = bytes.size();
                        PROFILE_LOGGER.debug("key: {} value size: {} bytes", code, size);
                    } catch (final IOException e) {
                        NodeNetworkManager.PROFILE_LOGGER
                                .error("Error constructing byte stream for measurement of object", e);
                    }
                }
                unwrapAndLog(0, o);
                PROFILE_LOGGER.debug("Finished logging {}", code);
            });
        }

        final Map<DeviceUID, NetworkNeighbor> toRemove = nbrsCopy.entrySet().stream().map(entry -> {
            final NetworkNeighbor neighbor = entry.getValue();
            if (!neighbor.isRunning()) {
                LOGGER.warn("{} is not running, removing from the list of active connections", entry.getKey());
                return entry;
            }

            LOGGER.trace("Sending message from {} to {}", node.getName(), entry.getKey());
            neighbor.shareApState(localSend);
            LOGGER.trace("Finished sending message from {} to {}", node.getName(), entry.getKey());
            return null;
        }).filter(e -> null != e).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!toRemove.isEmpty()) {
            synchronized (lock) {
                for (final Map.Entry<DeviceUID, NetworkNeighbor> entry : toRemove.entrySet()) {
                    entry.getValue().terminate();
                    nbrs.remove(entry.getKey());
                }
            }
        }

        LOGGER.debug("Bottom of share AP round {} sending from {}", node.getExecutionCount(), node);
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

        node.getApNeighbors().parallelStream().forEach(neighborUID -> {
            if (!nbrsCopy.containsKey(neighborUID)) {
                connectToNeighbor(neighborUID);
            }
        });
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

    /**
     * 
     * @return the set of neighbors that are currently connected to AP
     */
    public Set<NodeIdentifier> getConnectedNeighbors() {
        synchronized (lock) {
            return new HashSet<>(nbrs.keySet());
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
    private final Map<NodeIdentifier, NetworkNeighbor> nbrs = new HashMap<>();

    private void addNeighbor(final int nonce,
            final NodeIdentifier neighborId,
            final Socket s,
            final DataInputStream input,
            final DataOutputStream output) {
        synchronized (lock) {
            // symmetry-break nonce
            // If UID isn't already linked, add a new neighbor

            final InetSocketAddress remoteAddr = new InetSocketAddress(s.getInetAddress(), s.getPort());
            final NetworkNeighbor other = nbrs.get(neighborId);
            if (null == other || other.getNonce() < nonce) {
                if (null != other) {
                    LOGGER.debug("Closing remote connection from {} because there's a connection to them", neighborId);
                    other.terminate();
                }

                final NetworkNeighbor neighbor = new NetworkNeighbor(node, neighborId, nonce, remoteAddr, s, input,
                        output);
                nbrs.put(neighborId, neighbor);
                neighbor.start();
                LOGGER.debug("Started new neighbor connection with {}", neighborId);
            } else {
                LOGGER.debug("Closing connection to {} because we already have a connection from them", neighborId,
                        node.getNodeIdentifier());
                try {
                    writeCloseConnection(output);
                } catch (final IOException e) {
                    LOGGER.debug("Error writing close to neighbor, ignoring", e);
                } catch (final Exception e) {
                    LOGGER.debug("Unepxected error writing close to neighbor, ignoring", e);
                }

                try {
                    input.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing input to neighbor, ignoring", e);
                }

                try {
                    output.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing output to neighbor, ignoring", e);
                }

                try {
                    s.close();
                } catch (final IOException e) {
                    LOGGER.debug("Error closing socket to neighbor, ignoring", e);
                }
            }
        } // lock so that we don't add 2 connections to the neighbor
    }

    /**
     * Write the message that the connection should close.
     * 
     * @param output
     *            where to write the message
     * @throws IOException
     *             if there is an error writing to {@code output}
     */
    public static void writeCloseConnection(final DataOutputStream output) throws IOException {
        output.write(MESSAGE_TYPE_CLOSE);
        output.flush();
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

                        LOGGER.debug("Got a connection from {}", s.getRemoteSocketAddress());

                        // don't need a thread here since addNeighbor will take
                        // care of creating a thread to service the connection.
                        try {
                            final DataOutputStream output = new DataOutputStream(s.getOutputStream());
                            final DataInputStream input = new DataInputStream(s.getInputStream());

                            // If the link connects, trade UIDs

                            // write uid for neighbor
                            LOGGER.trace("Writing node identifier to new connection");
                            writeHello(output, RANDOM.nextInt(), node.getNodeIdentifier());

                            // reads data from connectToNeighbor()
                            LOGGER.trace("Reading node identifier and nonce from neighbor {}",
                                    s.getRemoteSocketAddress());
                            final byte remoteMessageType = input.readByte();
                            if (remoteMessageType == MESSAGE_TYPE_HELLO) {
                                final HelloMessage remoteHello = HelloMessage.readMessage(input);

                                addNeighbor(remoteHello.getNonce(), remoteHello.getId(), s, input, output);
                                LOGGER.trace("Received uid {} and nonce {} from {}", remoteHello.getId(),
                                        remoteHello.getNonce(), s.getRemoteSocketAddress());
                            } else {
                                LOGGER.error("Unexpected message type from neighbor: "
                                        + String.format("%02x", remoteMessageType));
                                s.close();
                            }
                        } catch (final IOException e) {
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
     * 
     * @param output
     *            where to write the message
     * @param nonce
     *            see {@link HelloMessage#getNonce()}
     * @param id
     *            see {@link HelloMessage#getId()}
     * @throws IOException
     *             if there is an error writing to or flushing the stream
     */
    public static void writeHello(final DataOutputStream output, final int nonce, final NodeIdentifier id)
            throws IOException {
        final HelloMessage localHello = new HelloMessage(id, nonce);
        output.writeByte(MESSAGE_TYPE_HELLO);
        localHello.writeMessage(output);
        output.flush();
    }

    /**
     * Attempt to create a connection to a neighbor.
     * 
     * @param neighborNode
     *            the neighbor to connect to
     */
    private void connectToNeighbor(final NodeIdentifier neighborUID) {
        final InetSocketAddress addr = lookupService.getInetAddressForNode(neighborUID);
        if (null == addr) {
            LOGGER.warn(neighborUID
                    + " is not found in the lookup service, not connecting to this neighbor for AP sharing");
            return;
        }

        LOGGER.debug("Connecting to {} from {}", neighborUID, node.getNodeIdentifier());

        try {
            // Try to link
            final Socket s = new Socket(addr.getAddress(), addr.getPort());

            final int nonce = RANDOM.nextInt();

            final DataOutputStream output = new DataOutputStream(s.getOutputStream());
            final DataInputStream input = new DataInputStream(s.getInputStream());

            // If the link connects, trade UIDs
            writeHello(output, nonce, node.getNodeIdentifier());

            LOGGER.debug("Reading identifier from neighbor {}", node.getNodeIdentifier());
            final byte remoteMessageType = input.readByte();
            if (remoteMessageType == MESSAGE_TYPE_HELLO) {
                final HelloMessage remoteHello = HelloMessage.readMessage(input);

                addNeighbor(nonce, remoteHello.getId(), s, input, output);
            } else {
                LOGGER.error("Unexpected message type connecting to neighbor {}: {}", neighborUID,
                        String.format("%02x", remoteMessageType));
                s.close();
            }
        } catch (final Exception e) {
            LOGGER.debug("Couldn't connect to neighbor: {} at {}. Will try again later.", neighborUID, addr, e);
        }
    }

    /**
     * Message type for {@link ShareDataMessage}.
     */
    public static final byte MESSAGE_TYPE_AP_SHARE = 1;
    /**
     * Message type for {@link HelloMessage}.
     */
    public static final byte MESSAGE_TYPE_HELLO = 2;
    /**
     * Message type for closing of the connection.
     */
    public static final byte MESSAGE_TYPE_CLOSE = 3;

}
