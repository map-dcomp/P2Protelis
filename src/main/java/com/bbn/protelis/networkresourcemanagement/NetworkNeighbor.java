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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.protelis.vm.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network link between a {@link NetworkServer} and it's neighbor. Used to
 * send and receive data.
 */
/* package */
final class NetworkNeighbor {

    private final Logger logger;

    @GuardedBy("sharedValuesLock")
    private Map<CodePath, Object> sharedValues = new HashMap<>();
    private final Object sharedValuesLock = new Object();

    /**
     * @return The data most recently shared from the remote
     *         {@link NetworkServer}. Not null.
     */
    public Map<CodePath, Object> getSharedValues() {
        synchronized (sharedValuesLock) {
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

    private final DataInputStream input;
    @GuardedBy("sendLock")
    private final DataOutputStream output;
    private final Socket socket;
    private final Object sendLock = new Object();

    private final Thread readThread;
    private final Thread sendThread;

    /* package */ NetworkNeighbor(final NetworkServer selfNode,
            final NodeIdentifier neighborUid,
            final int nonce,
            final InetSocketAddress addr,
            final Socket s,
            final DataInputStream input,
            final DataOutputStream output) {
        final String baseName = String.format("%s_to_%s_port_%d", selfNode.getNodeIdentifier(), neighborUid,
                addr.getPort());

        logger = LoggerFactory.getLogger(NetworkNeighbor.class.getName() + "." + baseName);

        readThread = new Thread(this::readData, baseName + "-receive");
        sendThread = new Thread(this::sendData, baseName + "-send");

        this.input = input;
        this.output = output;
        this.socket = s;
        this.nonce = nonce;
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
     * Start reading and writing on the socket.
     * 
     * @throws IllegalThreadStateException
     *             if the threads are already running
     */
    public void start() {
        if (isRunning()) {
            throw new IllegalThreadStateException("Already running");
        }
        running.set(true);
        readThread.start();
        sendThread.start();
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
    private void readData() {
        try {
            while (running.get()) {
                final byte messageType = input.readByte();
                if (NodeNetworkManager.MESSAGE_TYPE_AP_SHARE == messageType) {
                    final ShareDataMessage msg = ShareDataMessage.readMessage(input);

                    if (!simulateDroppedMessage()) {
                        synchronized (sharedValuesLock) {
                            sharedValues = applyDelta(msg.getData());
                        }
                    }
                } else if (NodeNetworkManager.MESSAGE_TYPE_CLOSE == messageType) {
                    logger.debug("Received close connection message, exiting");
                    break;
                } else {
                    logger.error("Received unexpected message type ({}), assuming corrupted stream and exiting",
                            String.format("%02x", messageType));
                    break;
                }
            }
        } catch (final StreamSyncLostException e) {
            logger.error("Lost sync of the AP stream", e);
        } catch (final OptionalDataException e) {
            logger.error("failed to read data from neighbor. eof: " + e.eof + " length: " + e.length, e);
        } catch (final IOException e) {
            if (!running.get()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failed to receive from neighbor (in shutdown)", e);
                }
            } else {
                logger.error("failed to receive from neighbor", e);
            }
        } catch (final Exception e) {
            logger.error("unexpected exception receiving", e);
        } finally {
            terminate();
        }
    }

    /**
     * Terminate the connection.
     */
    public void terminate() {
        if (!running.get()) {
            // already shutdown or in terminate already
            return;
        }

        logger.debug("Terminating connection");

        synchronized (sendLock) {
            try {
                NodeNetworkManager.writeCloseConnection(output);
            } catch (final IOException e) {
                logger.debug("Got error writing close message, ignoring", e);
            } catch (final Exception e) {
                logger.error("Unexpected exception writing close message, ignoring", e);
            }
        }

        running.set(false);
        readThread.interrupt();
        sendThread.interrupt();

        try {
            input.close();
        } catch (final IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got error closing input stream on shutdown, ignoring.", e);
            }
        }

        try {
            output.close();
        } catch (final IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got error closing output stream on shutdown, ignoring.", e);
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

    private final Object apDataLock = new Object();
    @GuardedBy("apDataLock")
    private Map<CodePath, Object> nextApStateToShare = null;

    /**
     * Share some state with this neighbor. This state will be shared as soon as
     * the network is available.
     * 
     * @param stateToShare
     *            the state to be shared
     */
    public void shareApState(final Map<CodePath, Object> stateToShare) {
        synchronized (apDataLock) {
            nextApStateToShare = stateToShare;
            apDataLock.notifyAll();
        }
    }

    private void sendData() {
        try {
            while (running.get()) {
                final Map<CodePath, Object> toSend;

                synchronized (apDataLock) {
                    while (running.get() && null == nextApStateToShare) {
                        try {
                            logger.trace("Waiting for AP data");
                            apDataLock.wait();
                        } catch (final InterruptedException interrupted) {
                            logger.debug("Got interrupted waiting for AP data to send", interrupted);
                        }
                    }
                    toSend = nextApStateToShare;
                    nextApStateToShare = null;
                }

                if (null != toSend) {
                    logger.trace("Sending ap state");
                    sendApState(toSend);
                }
            }
        } catch (final IOException e) {
            if (!running.get()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failed to send to neighbor (in shutdown)", e);
                }
            } else {
                logger.error("failed to send to neighbor", e);
            }
        } catch (final Exception e) {
            logger.error("unexpected exception sending", e);
        } finally {
            logger.info("Terminating sendData");
            terminate();
        }
    }

    private Map<CodePath, Object> previouslySentState = new HashMap<>();

    private static final String AP_STATE_DELETE_KEY = "delete-key";

    private Map<CodePath, Object> applyDelta(final Map<CodePath, Object> receivedData) {
        if (GlobalNetworkConfiguration.getInstance().getUseDeltaCompression()) {
            final Map<CodePath, Object> newShared = getSharedValues();
            receivedData.forEach((codePath, newValue) -> {
                if (AP_STATE_DELETE_KEY.equals(newValue)) {
                    newShared.remove(codePath);
                } else {
                    newShared.put(codePath, newValue);
                }
            });
            return newShared;
        } else {
            return receivedData;
        }
    }

    private Map<CodePath, Object> doDeltaCompression(final Map<CodePath, Object> toSend) {
        if (GlobalNetworkConfiguration.getInstance().getUseDeltaCompression()) {
            // use parallel stream in case the equals implementation is slow
            final Map<CodePath, Object> newData = toSend.entrySet().parallelStream() //
                    .map(entry -> {
                        final CodePath path = entry.getKey();
                        final Object data = entry.getValue();
                        if (previouslySentState.containsKey(path)) {
                            final Object prevData = previouslySentState.get(path);
                            if (Objects.equals(prevData, data)) {
                                // if the data hasn't changed, don't send it
                                return null;
                            } else {
                                return entry;
                            }
                        } else {
                            return entry;
                        }
                    }) //
                    .filter(e -> null != e) //
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            previouslySentState.forEach((codePath, value) -> {
                if (!toSend.containsKey(codePath)) {
                    newData.put(codePath, AP_STATE_DELETE_KEY);
                }
            });
            return newData;
        } else {
            return toSend;
        }
    }

    private void sendApState(final Map<CodePath, Object> fullToSend) throws IOException {

        // The encoding of the message is done here rather than in
        // NodeNetworkManager so that we can do delta
        // compression on the Map.
        final Map<CodePath, Object> deltaToSend = doDeltaCompression(fullToSend);

        logger.debug("Start encode AP data");
        final ShareDataMessage message = new ShareDataMessage(deltaToSend);
        logger.debug("End encode AP data");

        logger.debug("Start send of AP data");
        sendMessage(NodeNetworkManager.MESSAGE_TYPE_AP_SHARE, message);
        logger.debug("End send of AP data");

        // if sendMessage didn't throw an exception, we assume that the state
        // has been sent
        previouslySentState = fullToSend;
    }

    /**
     * Send a message to this neighbor.
     * 
     * @param toSend
     *            what to send
     * @throws IOException
     *             when there is an error writing
     */
    private void sendMessage(final byte messageType, final ApMessage msg) throws IOException {
        if (running.get()) {
            synchronized (sendLock) {
                output.writeByte(messageType);
                msg.writeMessage(output);

                logger.trace("sendMessage is calling flush");
                output.flush();

                logger.trace("sendMessage finished");
            }
        }
    }

}
