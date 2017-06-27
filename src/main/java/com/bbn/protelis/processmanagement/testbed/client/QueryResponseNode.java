package com.bbn.protelis.processmanagement.testbed.client;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.protelis.lang.datatype.Tuple;
import org.protelis.lang.datatype.Tuples;
import org.protelis.lang.datatype.impl.ArrayTupleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.daemon.LocalDaemon;

import cern.colt.Arrays;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public class QueryResponseNode extends CrumpleZoneMonitorable {
    private Logger logger = LoggerFactory.getLogger("QueryResponseNode");
    
    // This set of variables are expected to be configured from JSON serialization
    /** Should be a comparable uid; might be a number or a name or anything else. */
    protected Object identifier = "";
    private String shortName = "";
    /** The port where the QRNode will serve connections from others */
    private int port;
    private Object[][] dependencyList = new Object[0][];
    private boolean originator;
    private AttackModel attackModel = new NullAttackModel(); // by default, no attacks
    
    /** State of the process: initially not running */
    private ProcessStatus status = ProcessStatus.stop;
    /** Interval between new queries for originators */
    private int QINTERVAL = 10000; //5000000; // 5000
    private int QINITIAL = 10000; // 2000
    /** Intervals for response times */
    private static final long LOCAL_DELAY = 1000;
    private static final long RELAY_DELAY = 300;
    private static final long POLL_FREQUENCY = 100;
    /** Time before a query is dropped for lack of response */
    //private final int QUERY_DROP_TIME = 5000;
    /** Live dependencies processed from the original dependencyList */
    private Tuple dependencies = new ArrayTupleImpl();
    private boolean firstInit = true;

    private class State {
        private Set<DependencyQuery> infections = new HashSet<>();
        private BlockingQueue<Event> processingQueue = new PriorityBlockingQueue<>();
        private Map<Object,PendingQueryRecord> pendingQueries = new ConcurrentHashMap<>();
        private int resolvedQueries = 0;
        
        private void checkpoint() {
            State clone = new State();
            clone.infections.addAll(infections);
            clone.processingQueue.addAll(processingQueue);
            clone.pendingQueries.putAll(pendingQueries);
            clone.resolvedQueries = resolvedQueries;
            stateRecord.push(clone);
        }
        
        protected void registerIncoming(final DependencyQuery q) {
            synchronized (this) {
                checkpoint();
                q.type = Event.EventType.INCOMING;
                recordInteraction(q.sourceAddr, q.sourcePort, new Message(true,q));
                processingQueue.add(q);
            }
        }
        protected void registerResponse(final DependencyQuery q) {
            synchronized (this) {
                checkpoint();
                recordInteraction(q.destinationAddr, q.destinationPort, new Message(false,q));
                pendingQueries.remove(q.sessionID);
                resolvedQueries++;
            }
        }
        protected void registerQuery(final DependencyQuery q) {
            synchronized (this) {
                checkpoint();
                recordInteraction(q.destinationAddr, q.destinationPort, new Message(false,q));
            }
        }
        
        protected Event getNextEvent() throws InterruptedException {
            // Check if there's an event ready for consumption
            Event e = processingQueue.peek();
            //logger.trace("Considering event: "+e);
            if (e == null || e.time >= System.currentTimeMillis()) {
                //logger.trace("Sleeping: event before scheduled time:"+e.time);
                Thread.sleep(POLL_FREQUENCY);
                return null;
            }
            // If so, pull the event, checkpoint for rewinding if necessary, and proceed
            e = processingQueue.poll();
            logger.trace(identifier + " processing event " + e);
            return e;
        }
        
        protected void createQuery(final DependencyQuery q) {
            UUID session = UUID.randomUUID();
            for (Object d : dependencies) {
                InetAddress dstAddr = (InetAddress)((Tuple)d).get(0);
                int dstPort = (int)((Tuple)d).get(1);
                DependencyQuery subq = new DependencyQuery(session,dstAddr,dstPort,true,null,port);
                exportInfections(subq,q);  subq.originName = q.originName;
                processingQueue.add(subq);
            }
            pendingQueries.put(session, new PendingQueryRecord(q));
        }
        
        private void processResponse(final DependencyQuery q) {
            PendingQueryRecord pqr = pendingQueries.get(q.sessionID);
            if (pqr == null) {
                logger.debug("Ignoring unmatched response: " + q);
                return; // just drop nonsense packets
            }
            if (pqr.incorporateResponse(q)) {
                if (!pqr.stimulus.isOrigin()) {
                    scheduleResponse(RELAY_DELAY,pqr.stimulus);
                    pendingQueries.remove(q.sessionID);
                } else {
                    resolvedQueries++;
                    logger.info("Query resolved: " + q.sessionID);
                    pendingQueries.remove(q.sessionID);
                }
            }
        }
        
        protected void localQueryResponse(final DependencyQuery q) {
            pendingQueries.put(q.sessionID, new PendingQueryRecord(q));
            scheduleResponse(LOCAL_DELAY,q); // If no dependencies, immediate response
        }
        
        protected void scheduleResponse(final long delay, final DependencyQuery q) {
            DependencyQuery r = new DependencyQuery(q.sessionID,q.sourceAddr,q.sourcePort,false,q.destinationAddr,q.destinationPort);
            exportInfections(r,q);  r.originName = q.originName;
            ScheduledResponse sr = new ScheduledResponse(delay,r);
            processingQueue.add(sr);
        }
        
        private void importInfections(final DependencyQuery q) {
            for (Attack a : q.attacks) {
                if (a.apply(QueryResponseNode.this)) {
                    infections.add(q);
//                    q.infective = true;
                }
            }
            
        }
        
        private void exportInfections(final DependencyQuery q, final DependencyQuery parent) {
            if (!infections.isEmpty() || isContaminated()) {
                q.contaminated = true;
            }
            q.attacks = parent.attacks;
        }
        
        /******* Accessors *******/
        public Set<DependencyQuery> getInfections() { 
            return infections;
        }
        public int getResolvedQueries() { 
            return resolvedQueries;
        }
        public Set<Object> getPendingQueries() { 
            return pendingQueries.keySet(); 
        }
        public BlockingQueue<Event> getProcessingQueue() { 
            return processingQueue; 
        }
        public boolean isCompromised() { 
            return !infections.isEmpty(); 
        }

    }
    private State currentState = new State();
    private Stack<State> stateRecord = new Stack<>();
    private Stack<DependencyQuery> incomingReplays = new Stack<>();
    
    private boolean processing = true, running = true;
    private Thread serveThread, processThread, originatorThread;
    
    private static class Event implements Comparable<Event>, Serializable {
        private static final long serialVersionUID = 8349453882826003944L;
        
        public enum EventType { INCOMING, QUERY, RESPONSE, OTHER };
        EventType type = EventType.OTHER;
        
        public long time = System.currentTimeMillis();
        private static int count = 0;
        public int index = count++;
        
        @Override 
        public int hashCode() {
            return (int)time;
        }
        
        @Override
        public boolean equals(final Object o) {
            if(o instanceof Event) {
                return 0 == compareTo((Event)o);
            } else {
                return super.equals(o);
            }
        }
        
        @Override
        public int compareTo(final Event o) {
            if (time == o.time) {
                return Integer.compare(index,  o.index);
            } else {
                return Long.compare(time,  o.time);
            }
        }
        
    }
    public static class DependencyQuery extends Event implements Serializable {
        private static final long serialVersionUID = 7492882723381042501L;

        public DependencyQuery(final Object session, final InetAddress destination, final int dstPort, final boolean query, final InetAddress source, final int srcPort) {
            sessionID = session;
            destinationAddr = destination; destinationPort = dstPort; 
            this.query = query; 
            sourceAddr = source; sourcePort = srcPort; 
        }
//        private UUID packet_id = UUID.randomUUID();
        private Object sessionID; // persistent ID for a query-response chain
        private String originName = ""; // optional tag for debugging
        // To/from information
        private InetAddress destinationAddr;
        private int destinationPort;
        private InetAddress sourceAddr;
        private int sourcePort;
        private boolean query;
        // Attack and contamination model:
        private Set<Attack> attacks = new HashSet<>();
        private boolean contaminated = false;
//        private boolean infective = false;
        // no payload, since just a dummy
        
        private boolean isOrigin() { 
            return sourcePort < 0;
        }

        @Override
        public String toString() {
            //return "M"+index;
            return (query ? "Q" : "R") + originName + ":" + index;
            //return (query?"Query":"Response")+": "+source+" ("+packet_id+")";
//          return (query?"Q":"R")
//                  +"<"+sourceAddr+":"+sourcePort+"> -> "
//                  +"<"+destinationAddr+":"+destinationPort+">"
//                  +packet_id.toString().substring(0,2);
        }
    }
    
    private static class ScheduledResponse extends Event {
        private static final long serialVersionUID = -2458974602097535239L;
        
        private DependencyQuery q;
        ScheduledResponse(final long delay,final DependencyQuery q) {
            super(); 
            this.q = q; time += delay;
        }
        public String toString() { 
            return q.toString();
        }
        
        public int hashCode() { 
            return q.hashCode();
        }
        
        @SuppressFBWarnings(value="EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS", justification="Checking against a sub-object, probalby OK, but kind of dodgy")
        public boolean equals(final Object o) {
            if (o instanceof ScheduledResponse) {
                return q.equals(((ScheduledResponse) o).q);
            } else if (o instanceof DependencyQuery) {
                return q.equals(o);
            } else {
                return false;
            }
        }
    }
    
    private class PendingQueryRecord {
        PendingQueryRecord(final DependencyQuery stimulus) { 
            //timeout = System.currentTimeMillis() + QUERY_DROP_TIME;
            satisfied = Tuples.fill(false, dependencies.size());
            this.stimulus = stimulus;
        }
        //public long timeout;
        public Tuple satisfied;
        public DependencyQuery stimulus;
        
        // Returns true if all satisfied
        public boolean incorporateResponse(final DependencyQuery q) {
            boolean allSatisfied = true;
            for (int i = 0; i < dependencies.size(); i++) {
                Tuple dep = (Tuple)dependencies.get(i);
                if (q.sourceAddr.equals(dep.get(0)) && dep.get(1).equals(q.sourcePort)) {
                    satisfied = satisfied.set(i, true);
                }
                if (!(Boolean)satisfied.get(i)) {
                    allSatisfied = false;
                }
            }
            return allSatisfied;
        }
        
        public String toString() {
            return "<PQR: " + stimulus + ":" + satisfied + ">";
        }
    }

    private void sleepIfNotProcessing() {
        while (!processing) { 
            try {
                Thread.sleep(POLL_FREQUENCY);
            } catch (InterruptedException e) {
                // ignore interruptions
            }
        }
        restoreReplays();
    }
    
    private void processQueue() {
        Thread.currentThread().setName(identifier + "-processing");
        while (running) {
            // Process head of queue
            try {
                Event e = currentState.getNextEvent();
                if (e == null) {
                    continue;
                }
                // otherwise, process now
                if (e instanceof DependencyQuery) {
                    DependencyQuery q = (DependencyQuery)e;
                    // Check for infections
                    if (!fromSelf(q)) { 
                        currentState.importInfections(q);
                    }
                    // Case depends on nature of DQ packet:
                    if (fromSelf(q)) { 
                        transmitQuery(q); // queued query packet to send
                    } else if (q.query) { // Received a query:
                        if (dependencies.isEmpty()) { 
                            currentState.localQueryResponse(q);
                        } else { 
                            currentState.createQuery(q); 
                        } // Otherwise, queue up a bunch of query packets
                    } else { 
                        currentState.processResponse(q); // Response to be processed, possibly sending a packet
                    }
                } else if (e instanceof ScheduledResponse) {
                    transmitResponse(((ScheduledResponse)e).q);
                }
            } catch (InterruptedException e) {
                /* ignore */
            }
            // Optional pause
            sleepIfNotProcessing();
        }
    }
    
    private boolean fromSelf(final DependencyQuery q) {
        return q.sourceAddr == null && q.sourcePort == port;
    }
    
    private void transmitQuery(final DependencyQuery q) {
        currentState.registerQuery(q);
        Socket s = null;
        try {
            s = new Socket(q.destinationAddr,q.destinationPort);
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(q);
        } catch (NotSerializableException e) {
            logger.debug("Tried to send non-serializable query: " + e);
        } catch (IOException e) {
            logger.debug("Couldn't send query: " + q);
        } finally {
            if (s != null) {
                try { 
                    s.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }
    }
    

    private void transmitResponse(final DependencyQuery r) {
        currentState.registerResponse(r);
        Socket s = null;
        try {
            s = new Socket(r.destinationAddr,r.destinationPort);
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(r);
        } catch (NotSerializableException e) {
            logger.debug("Tried to send non-serializable response: " + e);
        } catch (IOException e) {
            logger.debug("Couldn't send response: " + r);
        } finally {
            if (s != null) { 
                try { 
                    s.close(); 
                } catch (IOException e) { 
                    /* ignore */ 
                }
            }
        }
    }
    
    protected int sessionID = 0;
    private void originateQueries() {
        try { 
            Thread.sleep(QINITIAL); 
        } catch (InterruptedException e) { 
            /* ignore */ 
        }
        
        while (running) {
            String id = shortName + ":" + (sessionID++);
            DependencyQuery q = new DependencyQuery(id,null, -1, true, null, -1);
            q.attacks = attackModel.attackInstanceFor(this); q.originName = shortName;
            currentState.registerIncoming(q);
            try { 
                Thread.sleep(QINTERVAL); 
            } catch (InterruptedException e) { 
                /* ignore */ 
            }
        }
    }
    /**
     * Lightweight server that receives DependencyQuery packets and pushes them into the processingQueue
     */
    private void runServer() {
        Thread.currentThread().setName(identifier + "-server");
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            logger.info(identifier + ": listening on port " + port);
            // Each connection sends a single DependencyQuery and then terminates
            while (running) {
                Socket conn = server.accept();
                ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
                DependencyQuery q = (DependencyQuery)in.readObject();
                // Fill in InetAddress it arrived from:
                q.sourceAddr = conn.getInetAddress();
                currentState.registerIncoming(q);
            }
        } catch (BindException e) {
            logger.debug("Port already in use.");
            crash();
        } catch (Exception e) {
            logger.debug("Server crashed while trying to read objects");
            crash();
        } finally {
            if (server != null) {
                try { 
                    server.close(); 
                } catch (IOException e) { 
                    /* ignore */ 
                }
            }
        }
    }

    
    private void initializePersistentState() {
        port += LocalDaemon.testPortOffset;
        try {
            List<Tuple> dlist = new ArrayList<>();
            for (Object[] d : dependencyList) {
                if (!(d.length == 2 && d[0] instanceof String && d[1] instanceof Number)) {
                    throw new AssertionError("Dependency list not [string,integer]: " + Arrays.toString(d));
                }
                InetAddress depAddr = InetAddress.getByName((String) d[0]);
                int depPort = LocalDaemon.testPortOffset + ((Number)d[1]).intValue();
                dlist.add(new ArrayTupleImpl(depAddr,(Object)depPort));
            }
            dependencies = new ArrayTupleImpl(dlist);
        } catch (UnknownHostException e) {
            // ignore, just end up with no dependencies
            // TODO: actually report this problem
        }
    }
    
    
    @Override
    public boolean init() {
        // Run first-time init if appropriate
        if (firstInit) { 
            initializePersistentState(); 
            firstInit = false; 
        }
        
        if (status != ProcessStatus.stop && status != ProcessStatus.init) {
            logger.info("Query Response Node '" + identifier + "' cannot initialize: not stopped or init'ing");
            return false;
        }
        logger.info("Query Response Node '" + identifier + "' initializing");
        
        status = ProcessStatus.init;
        
        serveThread = new Thread(() -> { 
            runServer(); 
        }); serveThread.start();
        processThread = new Thread(() -> { 
            processQueue(); 
        }); processThread.start();
        if (originator) { 
            originatorThread = new Thread(() -> { 
                originateQueries(); 
            }); originatorThread.start();
        }

        logger.info("Query Response Node '" + identifier + "': running");
        status = ProcessStatus.run;
        return true;
    }

    @Override
    public ProcessStatus getStatus() { 
        // On poll, test to see if node has crashed
        if (status == ProcessStatus.run) {
            if (!serveThread.isAlive()) {
                status = ProcessStatus.hung;
            }
            if (!processThread.isAlive()) {
                status = ProcessStatus.hung;
            }
            if (originatorThread != null && !originatorThread.isAlive()) {
                status = ProcessStatus.hung;
            }
        }
        // return compromised if set of attacks is non-empty, 
        if (status == ProcessStatus.run) {
            if (isCompromised()) {
                return ProcessStatus.compromised;
            }
            if (isContaminated()) {
                return ProcessStatus.contaminated;
            }
        }

        return status; 
    }
    
    @Override
    public boolean shutdown() {
        status = ProcessStatus.shutdown;
        logger.info("Query Response Node '" + identifier + "': shutdown");
        running = false;
        serveThread.interrupt();
        processThread.interrupt();
        if (originatorThread != null) {
            originatorThread.interrupt();
        }
        status = ProcessStatus.stop;
        logger.info("Query Response Node '" + identifier + "': stopped");
        return true;
    }

    @Override
    public Tuple knownDependencies() { 
        return dependencies;
    }
    @Override
    public int getCommPort() { 
        return port; 
    }
    
    @Override
    public boolean crash() {
        logger.info("Query Response Node '" + identifier + "': crashing");
        status = ProcessStatus.hung;
        return true;
    }
    
    /***** Accessors *****/
    public void setIsProcessing(final Boolean value) { 
        processing = value;
    }
    public Boolean isProcessing() { 
        return processing;
    }
    public Set<Object> getPendingQueries() { 
        return currentState.getPendingQueries(); 
    }
    public BlockingQueue<Event> getProcessingQueue() { 
        return currentState.getProcessingQueue(); 
    }

    @Override
    public boolean isCompromised() { 
        return currentState.isCompromised(); 
    }

    @Override
    public boolean isContaminated() {
        if (isCompromised()) {
            return true;
        }
        for (Message r : getRecord()) {
            DependencyQuery q = (DependencyQuery)r.getContents();
            if (q.contaminated) {
                return true;
            }
        }
        return false;
    }

    public Set<DependencyQuery> getInfections() { 
        return currentState.getInfections(); 
    }

    public void rewindOneStep(final Boolean discard) {
        // TODO: add retention of non-discards and lost pure incomings
        if (!stateRecord.isEmpty()) {
            super.rewindOneStep();
            currentState = stateRecord.pop();
        } else {
            currentState = new State();
        }
    }
    
    private void restoreReplays() {
        while (!incomingReplays.empty()) {
            DependencyQuery q = incomingReplays.pop();
            logger.info("Replaying arrival of: " + q);
            currentState.registerIncoming(q);
        }
    }
    
    public String reportString() {
        int done = currentState.getResolvedQueries();
        int total = done + currentState.getPendingQueries().size();
        return (processing ? "Normal" : "Rewind") + ": " + done + " of " + total + " queries served";
        //return (processing?"Normal":"Rewind")+": "+done+" queries served";
    }
}
