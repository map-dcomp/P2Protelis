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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.util.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network link between a {@link NetworkServer} and it's neighbor. Used to
 * send and receive data.
 */
/* package */
final class NetworkNeighbor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkNeighbor.class);

    private Map<CodePath, Object> sharedValues = new HashMap<>();

    /**
     * @return The data most recently shared from the remote
     *         {@link NetworkServer}. Not null.
     */
    public Map<CodePath, Object> getSharedValues() {
        synchronized (lock) {
            if (null == sharedValues) {
                return new HashMap<CodePath, Object>();
            } else {
                return new HashMap<CodePath, Object>(sharedValues);
            }
        }
    }

    /**
     * Used to keep from having parallel connections.
     * 
     * @return the value that was passed into the constructor.
     */
    /* package */ int getNonce() {
        return this.nonce;
    }

    private final int nonce;

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket socket;

    // use separate object rather than this to keep other objects from blocking
    // us
    private final Object lock = new Object();

    private final NetworkServer selfNode;
    private final DeviceUID neighborUid;

    /* package */ NetworkNeighbor(final NetworkServer selfNode,
            final DeviceUID neighborUid,
            final int nonce,
            final InetSocketAddress addr,
            final Socket s,
            final ObjectInputStream in,
            final ObjectOutputStream out) {
        super(String.format("%s -> %s:%s", selfNode.getDeviceUID(), neighborUid, addr.toString()));

        this.selfNode = selfNode;
        this.neighborUid = neighborUid;
        this.in = in;
        this.out = out;
        this.socket = s;
        this.nonce = nonce;
    }

    private long lastTouched = System.currentTimeMillis();

    /**
     * The last time this link sent or received a message.
     * 
     * @return milliseconds since epoch
     */
    public long getLastTouched() {
        synchronized (lock) {
            return lastTouched;
        }
    }

    private AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 
     * @return is this object still running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Listen for incoming packets
     */
    @Override
    public void run() {
        running.set(true);
        try {
            while (running.get()) {
                final Object incoming = in.readObject();
                if (incoming instanceof String) {
                    if (CLOSE_CONNECTION.equals(incoming)) {
                        LOGGER.debug("Received close connection message, exiting");
                        break;
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    final Map<CodePath, Object> shared = (incoming instanceof Map) ? (Map<CodePath, Object>) incoming
                            : null;

                    synchronized (lock) {
                        sharedValues = shared;
                        lastTouched = System.currentTimeMillis();
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            LOGGER.error(getName()
                    + ": Error decoding object from neighbor. This suggests that the JVMs are out of sync with respect to class objects",
                    e);
        } catch (final OptionalDataException e) {
            LOGGER.error(getName() + ": failed to read data from neighbor. eof: " + e.eof + " length: " + e.length, e);
        } catch (final IOException e) {
            if (!running.get()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(getName() + ": failed to receive from neighbor (in shutdown)", e);
                }
            } else {
                LOGGER.error(getName() + ": failed to receive from neighbor", e);
            }
        } finally {
            terminate();
        }
    }

    /**
     * Sent to signify that the connection should be closed. This is sent as an
     * object on the stream.
     */
    public static final String CLOSE_CONNECTION = "CLOSE_CONNETION";

    /**
     * Terminate the connection.
     */
    public void terminate() {
        LOGGER.debug("Terminating connection");

        try {
            out.writeObject(CLOSE_CONNECTION);
        } catch (final IOException e) {
            LOGGER.debug("Got error writing close message, ignoring", e);
        }

        running.set(false);
        interrupt();

        try {
            in.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing input stream on shutdown, ignoring.", e);
            }
        }
        try {
            out.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing output stream on shutdown, ignoring.", e);
            }
        }

        try {
            socket.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing socket on shutdown, ignoring.", e);
            }
        }
    }

    /**
     * Send a message to this neighbor.
     * 
     * @param toSend
     *            what to send
     * @throws IOException
     *             when there is an error writing
     */
    public void sendMessage(final Map<CodePath, Object> toSend) throws IOException {
        if (!isInterrupted() && running.get()) {
            if (NodeNetworkManager.PROFILE_LOGGER.isDebugEnabled()) {
                try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
                        oos.writeObject(toSend);
                    } catch (final IOException e) {
                        NodeNetworkManager.PROFILE_LOGGER.error("Error writing object to byte stream for measurement",
                                e);
                    }
                    final int size = bytes.size();
                    NodeNetworkManager.PROFILE_LOGGER.debug("AP round {} sending from {} to {} a message of {} bytes",
                            selfNode.getExecutionCount(), selfNode.getDeviceUID(), neighborUid, size);
                } catch (final IOException e) {
                    NodeNetworkManager.PROFILE_LOGGER.error("Error constructing byte stream for measurement", e);
                }
            }
            synchronized (lock) {
                out.writeObject(toSend);
                out.flush();
                lastTouched = System.currentTimeMillis();
                out.reset();
            }
        }
    }

}
