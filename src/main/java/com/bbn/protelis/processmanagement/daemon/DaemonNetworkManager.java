package com.bbn.protelis.processmanagement.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.util.CodePath;
import org.protelis.vm.NetworkManager;

// TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public class DaemonNetworkManager implements NetworkManager, Monitorable.Listener {
    /* Debugging */
    private Logger logger = null;
    // Existing links to neighbors, indexed by neighbor uid
    private final Map<Long,Neighbor> nbrs = new ConcurrentHashMap<>();
    // Map from current inetaddresses to links to neighbor uids
    private final Map<Pair<InetAddress,Integer>,Long> linkToNbr = new ConcurrentHashMap<>();
    private final Set<Pair<InetAddress,Integer>> pendingLinks = new HashSet<>();
    private final ExecutorService threads;
    private ServerSocket server;
    private final Monitorable client;
    private Daemon parent;
    boolean running = true;
    private final int port;
    
    /* ********************************* */
    /*          Constructors             */
    /* ********************************* */
    public DaemonNetworkManager(final Monitorable client, final long uid, final Logger log) throws UnknownHostException {
        assert (log != null);
        logger = log;
        
        port = (client == null) ? DaemonConstants.DEFAULT_PORT : client.getCommPort();
        
        // Set up naming for thread bundle
        String threadName = "Protelis-" + uid;
        final ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").build();
        threads = Executors.newCachedThreadPool(factory);
        
        // Connection creation and tracking:
        threads.execute(() -> listen_for_neighbors(port));
        if (client != null) {
            client.addListener(this);
        }
        this.client = client;
    }
    
    protected void setParent(final Daemon parent) { 
        this.parent = parent;
    }
    
    /* ********************************* */
    /*     Debug/control interface       */
    /* ********************************* */
    public void stop() {
        running = false;
        try { 
            server.close();
        } catch (Exception e) { 
            /* ignore: just cleanup */
        }
        nbrs.forEach((id,nbr) -> { 
            nbr.terminate();
        });
        threads.shutdownNow();
        try {
            boolean clean = threads.awaitTermination(5,TimeUnit.SECONDS);
            if (!clean) {
                logger.warn("Daemon: not all threads terminated cleanly.");
            }
        } catch (InterruptedException e) { 
            logger.warn("Daemon: interrupted during termination.");
        }
        logger.info("Daemon: terminated all threads.");
    }

    public Set<DeviceUID> getNeighbors() {
        return nbrs.keySet().stream().map((id) -> {
            return new LongDeviceUID(id);
        }).collect(Collectors.toSet()); 
    }

    /* ********************************* */
    /*        Accessors for VM           */
    /* ********************************* */
    Map<DeviceUID,Map<CodePath, Object>> nbrValues = new HashMap<>();
    @Override
    public Map<DeviceUID,Map<CodePath, Object>> getNeighborState() {
        nbrValues.clear();
        nbrs.forEach((id,nbr) -> {
            if (nbr.status == Neighbor.Status.running) {
                nbrValues.put(new LongDeviceUID(id), nbr.sharedValues);
            }
        });
        return nbrValues;
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        nbrs.forEach((id,nbr) -> {
            nbr.sendMessage(toSend);
        });
    }

    /* ********************************* */
    /*   Maintain Network Connections    */
    /* ********************************* */
    public void runCycle() throws UnknownHostException {
        probe_neighbors(); // attempt to add known dependencies
        prune_neighbors();
    }
    
    /**
     *  Remove stale neighbors
     */
    private void prune_neighbors() {
        long time = System.currentTimeMillis();
        for (Iterator<Entry<Long, Neighbor>> i = nbrs.entrySet().iterator(); i.hasNext();) {
            Neighbor nbr = i.next().getValue();
            boolean delete = false;
            if (nbr.status == Neighbor.Status.terminated) {
                logger.info("Protelis: pruning disconnected neighbor: " + nbr.name);
                delete = true;
            } else if (time - nbr.lastTouched > DaemonConstants.NBR_TIMEOUT) {
                logger.info("Protelis: pruning stale neighbor: " + nbr.name);
                delete = true;
            }
            // If we're deleting, delete all fragments of the relationship
            if (delete) {
                nbr.waitUntilTerminated(); // shut down the neighbor, if not already shut down
                // remove from IP maps
                for (Iterator<Entry<Pair<InetAddress, Integer>, Long>> i2 = linkToNbr.entrySet().iterator(); i2.hasNext();) {
                    Entry<Pair<InetAddress, Integer>, Long> e = i2.next();
                    if (e.getValue() == nbr.uid) {
                        i2.remove();
                    }
                }
                // And remove from neighbor list
                i.remove();
            }
        }
    }
    
    /**
     * Attempt to add neighbors from an "expected" list
     */
    private void probe_neighbors() throws NumberFormatException, UnknownHostException {
        Tuple dependencies = client.knownDependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            // Known dependencies returns a tuple of [addr,port]
            Tuple nbrDetails = (Tuple) dependencies.get(i);
            Pair<InetAddress,Integer> id = new Pair<>((InetAddress)nbrDetails.get(0),(Integer)nbrDetails.get(1));
            if (!linkToNbr.containsKey(id) && !pendingLinks.contains(id)) {
                logger.info("Protelis: probing neighbor: " + id.getFirst() + ":" + id.getSecond());
                threads.execute(() -> {
                    connectToNeighbor(id);
                });
            }
        }
    }
    
    /**
     * Add a neighbor observed to interact with the monitored process.
     */
    @Override
    public void identifyNeighbor(final Monitorable monitorable, final InetAddress nbrAddr, final int port) {
        // Insert neighbor if not already present
        Pair<InetAddress,Integer> id = new Pair<>(nbrAddr,port);
        if (!linkToNbr.containsKey(id) && !pendingLinks.contains(id)) {
            logger.info("Protelis: adding missing neighbor: " + nbrAddr + ":" + port);
            connectToNeighbor(id);
        }
        if (linkToNbr.containsKey(id)) {
            Neighbor nbr = nbrs.get(linkToNbr.get(id));
            if (nbr != null) {
                nbr.lastTouched = System.currentTimeMillis();
            }
        }
    }

    /**
     * Add neighbors by listening for those that contact us
     */
    private void listen_for_neighbors(final int port) {
        while (running) {
            try {
                int listenPort = port + DaemonConstants.DAEMON_PORT_OFFSET;
                server = new ServerSocket(listenPort);
                server.setReuseAddress(true);
                logger.info("Daemon listening for neighbors on port " + listenPort);
                while (running) {
                    Socket s = server.accept();
                    threads.execute(() -> { 
                        try {
                            // If the link connects, trade UIDs
                            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                            out.writeLong(parent.getId()); out.flush(); // Write own UID
                            long uid = in.readLong();       // Read other's UID
                            int rPort = in.readInt();   // Read other's port number
                            int nonce = in.readInt();       // Read other's symmetry-break nonce
                            // If UID isn't already linked, add a new neighbor
                            // Otherwise, we'll just note another name for the same neighbor
                            Pair<InetAddress,Integer> id = new Pair<>(s.getInetAddress(),rPort);
                            if (nbrs.get(uid) == null) {
                                Neighbor nbr = new Neighbor(uid,nonce,id.getFirst(),id.getSecond(),s,in,out,logger);
                                addNbr(nbr);
                            } else {
                                // Clean up the link
                                try { 
                                    out.close(); in.close(); s.close();
                                } catch (Exception e) {
                                    /* ignore: just cleanup */
                                }
                            }
                            // Given that all else is successful, note link in link table
                            linkToNbr.put(id, uid);
                        } catch (IOException e) {
                            /* Ignore IO Exceptions: they just mean we failed a link */
                        }
                    });
                }
            } catch (IOException e) {
                /* ignore IO exceptions: just keep trucking */
            } finally {
                try { 
                    server.close();
                } catch (Exception e2) {
                    /* ignore: just cleanup */
                }
            }
        }
    }
    
    /* ********************************* */
    /*  Implementation of network links  */
    /* ********************************* */
    
    static class Neighbor implements Runnable {
        /* Identity */
        private long uid;
        //@SuppressWarnings("unused") // TODO: determine whether we still need this
//        private int port;
        private final String name; // debug-friendly UID
        private final int nonce; // uid for connection (to symmetry-break multiple connections)
        /* Content */
        // TODO: check if we needed to share a program ID, of it the CodePath is sufficient
        Map<CodePath, Object> sharedValues = new HashMap<>();
        /* Link */
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private Socket socket = null;
        /* Run status */
        public enum Status { connecting, running, terminated, disconnected };
        private Status status = Status.connecting;
        public long lastTouched = System.currentTimeMillis();
        /* Debugging */
        private Logger logger;
        
        Neighbor(final long uid, final int nonce, final InetAddress host, final int port, final Socket s, final ObjectInputStream in, final ObjectOutputStream out, final Logger log) throws IOException {
            logger = log;
            this.uid = uid;
            this.nonce = nonce;
            name = host + ":" + port;
//            this.port = port;
            // Finish by linking the object streams
            socket = s;
            this.in = in; this.out = out;
        }
        
        /**
         * Listen for incoming packets
         */
        @Override
        public void run() {
            status = Status.running;
            try {
                while (status == Status.running) {
                    Object incoming = in.readObject();
                    //System.out.println("Protelis: receiving AST: " + incoming);
                    @SuppressWarnings("unchecked")
                    Map<CodePath, Object> shared = (incoming instanceof Map) ? (Map<CodePath, Object>) incoming : null;
                    sharedValues = shared;
                    lastTouched = System.currentTimeMillis();
                    //System.out.println("Protelis: successful packet from "+source+" "+rPort);
                }
            } catch (IOException e) {
                logger.error("Protelis: failed to receive from neighbor " + name);
                terminate();
            } catch (Exception e) {
                logger.error("",e);
            }
            // Wait to signal completion here, ensuring getting rid of concurrency
            status = Status.terminated;
        }

        public void sendMessage(final Map<CodePath, Object> toSend) {
            new Thread(() -> {
                try {
                    //System.out.println("Protelis: Sending AST: " + toSend);
                    out.writeObject(toSend);
                    out.flush();
                    lastTouched = System.currentTimeMillis();
                    //System.out.println("Protelis: successful packet to "+ipaddr+" "+portNum);
                } catch (IOException e) {
                    logger.error("Protelis: broken connection to neighbor " + name);
                    terminate();
                }
            }).start();
            
        }

        public void terminate() {
            try { 
                in.close();
            } catch (Exception e) { 
                /* ignore: just cleanup */
            }
            try { 
                out.close();
            } catch (Exception e) { 
                /* ignore: just cleanup */
            }
            try { 
                socket.close();
            } catch (Exception e) {
                /* ignore: just cleanup */
            }
        }
        
        public void waitUntilTerminated() {
            terminate();        
            while (status != Status.terminated) { 
                // TODO: do this more elegantly than a wait-loop
                try { 
                    Thread.sleep(10); 
                } catch (Exception e) {
                    // TODO: should we be doing something here?
                }
            }
        }
    }
    
    // This needs to be synchronized to make sure that we don't create parallel connections
    public synchronized void addNbr(final Neighbor nbr) {
        // If there are two connections, symmetry break by closing 
        // the one with the lower source-generated nonce
        Neighbor prior = nbrs.get(nbr.uid);
        if (prior == null || prior.nonce < nbr.nonce) {
            if (prior != null) { 
                prior.terminate();
            }
            nbrs.put(nbr.uid,nbr);
            threads.execute(nbr); // launch the neighbor that's been created
        } else {
            nbr.terminate();
        }
    }
    
    /**
     * Actually attempt to create a connection to a neighbor
     */
    boolean connectToNeighbor(final Pair<InetAddress,Integer> id) {
        pendingLinks.add(id);
        try {
            // Try to link
            final Socket s = new Socket(id.getFirst(), id.getSecond() + DaemonConstants.DAEMON_PORT_OFFSET);
            int nonce = UUID.randomUUID().hashCode();
            // If the link connects, trade UIDs
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            long uid = in.readLong();       // Read other's UID
            out.writeLong(parent.getId()); // Write own UID
            out.writeInt(port); // Write own port
            out.writeInt(nonce); // Write shared symmetry-break nonce for connection
            out.flush();
            // Given that all else is successful, note link in link table
            linkToNbr.put(id, uid);
            // If UID isn't already linked, add a new neighbor
            // Otherwise, we'll just note another name for the same neighbor
            if (nbrs.get(uid) == null) {
                Neighbor nbr = new Neighbor(uid,nonce, id.getFirst(),id.getSecond(),s,in,out,logger);
                addNbr(nbr);
            } else {
                // Clean up the link
                try { 
                    out.close(); in.close(); s.close();
                } catch (Exception e) {
                    /* Ignore, just cleanup */
                }
            }
        } catch (IOException e) {
            logger.info("Couldn't connect to neighbor " + id);
            return false;
            // couldn't connect to neighbor: don't worry about it
        }
        pendingLinks.remove(id);
        return true;
    }
}
