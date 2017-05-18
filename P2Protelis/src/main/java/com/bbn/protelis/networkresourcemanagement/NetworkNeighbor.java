package com.bbn.protelis.networkresourcemanagement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.util.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network link between a {@link Node} and it's neighbor. Used to send and
 * receive data.
 */
/* package */
final class NetworkNeighbor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkNeighbor.class);

    private Map<CodePath, Object> sharedValues = new HashMap<>();

    /**
     * @return The data most recently shared from the remote {@link Node}. Not
     *         null.
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

    /* package */ NetworkNeighbor(final ThreadGroup group, final DeviceUID uid, final int nonce,
            final InetSocketAddress addr, final Socket s, final ObjectInputStream in, final ObjectOutputStream out) {
        super(group, uid.toString() + addr.toString());

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

    /**
     * Listen for incoming packets
     */
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                final Object incoming = in.readObject();
                @SuppressWarnings("unchecked")
                final Map<CodePath, Object> shared = (incoming instanceof Map) ? (Map<CodePath, Object>) incoming
                        : null;

                synchronized (lock) {
                    sharedValues = shared;
                    lastTouched = System.currentTimeMillis();
                }
            }
        } catch (final IOException | ClassNotFoundException e) {
            LOGGER.error(getName() + ": failed to receive from neighbor", e);
        } finally {
            terminate();
        }
    }

    /**
     * Terminate the connection.
     */
    public void terminate() {
        interrupt();
        try {
            in.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing input stream, ignoring. Interrupted: " + isInterrupted(), e);
            }
        }
        try {
            out.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing output stream, ignoring. Interrupted: " + isInterrupted(), e);
            }
        }

        try {
            socket.close();
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got error closing socket, ignoring. Interrupted: " + isInterrupted(), e);
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
        if (!isInterrupted()) {
            synchronized (lock) {
                out.writeObject(toSend);
                out.flush();
                lastTouched = System.currentTimeMillis();
            }
        }
    }

}
