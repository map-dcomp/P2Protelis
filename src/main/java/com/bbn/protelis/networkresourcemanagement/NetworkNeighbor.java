/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020>, <Raytheon BBN Technologies>
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

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nustaq.net.TCPObjectSocket;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network link between a {@link NetworkServer} and it's neighbor. Used to
 * send and receive data.
 */
/* package */
final class NetworkNeighbor extends Thread {

    private final Logger logger;

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

    private final TCPObjectSocket serializer;
    private final Socket socket;

    // use separate object rather than this to keep other objects from blocking
    // us
    private final Object lock = new Object();

    /* package */ NetworkNeighbor(final NetworkServer selfNode,
            final DeviceUID neighborUid,
            final int nonce,
            final InetSocketAddress addr,
            final Socket s,
            final TCPObjectSocket serializer) {
        super(String.format("%s_to_%s_port_%d", selfNode.getNodeIdentifier(), neighborUid, addr.getPort()));
        logger = LoggerFactory.getLogger(NetworkNeighbor.class.getName() + "." + getName());

        this.serializer = serializer;
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

    private final Random random = new Random();

    private boolean simulateDroppedMessage() {
        final double messageDropPercentage = GlobalNetworkConfiguration.getInstance().getMessageDropPercentage();
        if (messageDropPercentage > 0) {
            final double value = random.nextDouble();
            if (value <= messageDropPercentage) {
                return true;
            }
        }
        return false;
    }

    /**
     * Listen for incoming packets
     */
    @Override
    public void run() {
        running.set(true);
        try {
            while (running.get()) {
                final Object incoming = serializer.readObject();
                if (incoming instanceof String) {
                    if (CLOSE_CONNECTION.equals(incoming)) {
                        logger.debug("Received close connection message, exiting");
                        break;
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    final Map<CodePath, Object> shared = (incoming instanceof Map) ? (Map<CodePath, Object>) incoming
                            : null;

                    if (!simulateDroppedMessage()) {
                        synchronized (lock) {
                            sharedValues = shared;
                            lastTouched = System.currentTimeMillis();
                        }
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            logger.error(getName()
                    + ": Error decoding object from neighbor. This suggests that the JVMs are out of sync with respect to class objects",
                    e);
        } catch (final OptionalDataException e) {
            logger.error(getName() + ": failed to read data from neighbor. eof: " + e.eof + " length: " + e.length, e);
        } catch (final IOException e) {
            if (!running.get()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getName() + ": failed to receive from neighbor (in shutdown)", e);
                }
            } else {
                logger.error(getName() + ": failed to receive from neighbor", e);
            }
        } catch (final Exception e) {
            logger.error(getName() + ": unexpected exception", e);
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
        logger.debug("Terminating connection");

        try {
            serializer.writeObject(CLOSE_CONNECTION);
        } catch (final IOException e) {
            logger.debug("Got error writing close message, ignoring", e);
        } catch (final Exception e) {
            logger.error("Unexpected exception writing close message, ignoring", e);
        }

        running.set(false);
        interrupt();

        try {
            serializer.close();
        } catch (final IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got error closing input stream on shutdown, ignoring.", e);
            }
        }

        try {
            socket.close();
        } catch (final IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got error closing socket on shutdown, ignoring.", e);
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
            synchronized (lock) {
                try {
                    logger.trace("sendMessage is calling writeObject num elements: {}", toSend.size());
                    serializer.writeObject(toSend);

                    logger.trace("sendMessage is calling flush");
                    serializer.flush();
                    lastTouched = System.currentTimeMillis();

                    logger.trace("sendMessage finished");
                } catch (final IOException e) {
                    throw e;
                } catch (final Exception e) {
                    // this should never come up as writeObject should only
                    // throw IOException
                    throw new RuntimeException("Unexpected exception writing object", e);
                }
            }
        }
    }

}
