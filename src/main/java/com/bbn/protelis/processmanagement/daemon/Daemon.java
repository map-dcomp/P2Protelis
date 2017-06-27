/**
 * 
 */
package com.bbn.protelis.processmanagement.daemon;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.random.BitsStreamGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.SimpleExecutionEnvironment;
import org.slf4j.Logger;

import com.bbn.protelis.utils.LongUID;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

/**
 * @author Danilo Pianini
 * @author Jacob Beal
 *
 */
public class Daemon extends AbstractExecutionContext {
    
    // Management of execution and networking
    private final long uid; // uid for self vs. neighbor
    private final LongUID deviceUID;
    private final ProtelisVM program;
    private final DaemonNetworkManager network;
    private final Monitorable client;
    private final BitsStreamGenerator rng = new MersenneTwister();
        
    // Management of debugging, reporting, and external control
    private Logger logger = null;
    private transient boolean running = false; // not running at start until launched
    private int executionCount = 0;
    Map<String,Object> debugs = new ConcurrentHashMap<>();
    Map<String,Object> lastDebugs = new ConcurrentHashMap<>();

    /* ********************************* */
    /*          Constructors             */
    /* ********************************* */
    //   Internal, fully-specified constructor
    private Daemon(final ProtelisProgram program, final boolean specifiedUID, final long uid, final Monitorable client, final Logger log) throws UnknownHostException {
        super(new SimpleExecutionEnvironment(), new DaemonNetworkManager(client,uid,log));
        this.client = client;
        network = (DaemonNetworkManager)this.getNetworkManager();
        network.setParent(this);
        
        assert (log != null);
        logger = log;
        
        this.uid = (specifiedUID ? uid : UUID.randomUUID().hashCode());
        this.deviceUID = new LongUID(this.uid);
        
        this.program = new ProtelisVM(program, this);
    }

    //   Construct with specified UID
    public Daemon(final ProtelisProgram program, final long uid, final Monitorable client, final Logger log) throws UnknownHostException {
        this(program,true,uid,client,log);
    }   
    //   Construct with random UID
    public Daemon(final ProtelisProgram program, final Monitorable client, final Logger log) throws UnknownHostException {
        this(program,false,0,client,log);
    }

    /* ********************************* */
    /*         Normal Run Cycle          */
    /* ********************************* */
    public void run() {
        running = true;
        while (running) {
            try {
                program.runCycle();
                executionCount++;
//              logger.debug("Protelis computed: uid=" + node.getId() + " round=" +executionCount + " value=" + currentValue());
                resetInternal();
                network.runCycle();
                notifyListeners();
                Thread.sleep(DaemonConstants.SLEEP_TIME);
            } catch (Exception e) {
                logger.error("Exception thrown: terminating Protelis");
                e.printStackTrace();
                running = false;
            }
        }
    }

    public interface Listener {
        void daemonUpdated(Daemon d);
    }
    ArrayList<Listener> listeners = new ArrayList<>();
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(final Listener listener) {
        listeners.remove(listener);
    }
    private void notifyListeners() {
        for (Listener l : listeners) { 
            l.daemonUpdated(this);
        }
    }

    public void stop() {
        running = false;
        network.stop();
    }

    /* ********************************* */
    /*  Debugging/Monitoring Interface   */
    /* ********************************* */
    public Object currentValue() {
        return program.getCurrentValue();
    }
    
    public Monitorable getClient() {
        return client;
    }
    public int getRound() {
        return executionCount;
    }
    
    public ProcessStatus getStatus() {
        if (running) {
            return ProcessStatus.run; 
        } else {
            return ProcessStatus.stop;
        }
    }
    
    public Set<DeviceUID> getNeighbors() {
        return network.getNeighbors();
    }
    
    public Object debug(final String token, final Object value) {
        debugs.put(token, value);
        return value;
    }
    // Reset debugging material for new round
    void resetInternal() {
        lastDebugs = debugs;
        debugs = new ConcurrentHashMap<>();
    }
    
    /* Passthroughs for Protelis calls */
    public Object getEnvironmentVariable(final String id) {
        return getExecutionEnvironment().get(id);
    }
    public Object getEnvironmentVariable(final String id, final Object defaultValue) {
        if (this.hasEnvironmentVariable(id)) {
            return getExecutionEnvironment().get(id);
        } else {
            return defaultValue;
        }
    }
    public void putEnvironmentVariable(final String id, final Object value) {
        getExecutionEnvironment().put(id,value);
    }
    public boolean hasEnvironmentVariable(final String id) {
        return getExecutionEnvironment().has(id);
    }
    

    /* ********************************* */
    /*        Required Functions         */
    /* ********************************* */

    /* ********* Implemented *********** */
    @Override
    public double nextRandomDouble() {
        return rng.nextDouble();
    }

    @Override
    public Number getCurrentTime() {
        return ((double)System.currentTimeMillis()) / 1000.0;
    }

    public long getId() {
        return uid;
    }

    @Override
    public DeviceUID getDeviceUID() {
        return this.deviceUID;
    }

    /* ********* Unimplemented ********* */
    @Override
    protected AbstractExecutionContext instance() {
        throw new UnsupportedOperationException();
    }
}
