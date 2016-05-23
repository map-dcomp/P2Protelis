/**
 * 
 */
package com.bbn.protelis.processmanagement.daemon;

import org.apache.commons.math3.random.BitsStreamGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.IProgram;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.ProtelisVM;

import it.unibo.alchemist.model.interfaces.IPosition;

import org.danilopianini.lang.util.FasterString;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Danilo Pianini
 * @author Jacob Beal
 *
 */
public class Daemon extends AbstractExecutionContext implements DeviceUID, Serializable {
	// UID for Serializable
	private static final long serialVersionUID = -7958084653258675854L;
	
	// Management of execution and networking
	private final long uid; // uid for self vs. neighbor
	private final ProtelisVM program;
	private final DaemonNetworkManager network;
	private final Monitorable client;
	private final BitsStreamGenerator rng = new MersenneTwister();
	private final Map<FasterString, Object> environment = new ConcurrentHashMap<>();
		
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
	private Daemon(IProgram program, boolean specifiedUID, long uid, Monitorable client, Logger log) throws UnknownHostException {
		super(new DaemonNetworkManager(client,uid,log));
		this.client = client;
		network = (DaemonNetworkManager)this.getNetworkManager();
		network.setParent(this);
		
		assert(log!=null);
		logger = log;
		
		this.uid = (specifiedUID ? uid : UUID.randomUUID().hashCode());
		
		this.program = new ProtelisVM(program, this);
	}

	//   Construct with specified UID
	public Daemon(IProgram program, long uid, Monitorable client, Logger log) throws UnknownHostException {
		this(program,true,uid,client,log);
	}	
	//   Construct with random UID
	public Daemon(IProgram program, Monitorable client, Logger log) throws UnknownHostException {
		this(program,false,0,client,log);
	}

	/* ********************************* */
	/*         Normal Run Cycle          */
	/* ********************************* */
	public void run() {
		running = true;
		while(running) {
			try {
				program.runCycle();
				executionCount ++;
//				logger.debug("Protelis computed: uid=" + node.getId() + " round=" +executionCount + " value=" + currentValue());
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
		public void daemonUpdated(Daemon d);
	}
	ArrayList<Listener> listeners = new ArrayList<>();
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	private void notifyListeners() {
		for(Listener l : listeners) { l.daemonUpdated(this); }
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
		if(running) return ProcessStatus.run; else return ProcessStatus.stop;
	}
	
	public Set<DeviceUID> getNeighbors() {
		return network.getNeighbors();
	}
	
	public Object debug(String token, Object value) {
		debugs.put(token, value);
		return value;
	}
	// Reset debugging material for new round
	void resetInternal() {
		lastDebugs = debugs;
		debugs = new ConcurrentHashMap<>();
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
		return ((double)System.currentTimeMillis())/1000.0;
	}

	@Override
	public Map<FasterString, Object> currentEnvironment() {
		return environment;
	}

	@Override
	protected void setEnvironment(Map<FasterString, Object> newEnvironment) {
		environment.putAll(environment);
	}

	public long getId() {
		return uid;
	}

	@Override
	public DeviceUID getDeviceUID() {
		return this;
	}

	/* ********* Unimplemented ********* */
	@Override
	public double distanceTo(DeviceUID target) {
		throw new UnsupportedOperationException();
		// Could also maybe be "1"?
	}

	@Override
	public IPosition getDevicePosition() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected AbstractExecutionContext instance() {
		throw new UnsupportedOperationException();
	}
}
