package com.bbn.protelis.processmanagement.testbed.daemon;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;

import com.bbn.protelis.processmanagement.daemon.Daemon;
import com.bbn.protelis.processmanagement.daemon.LongDeviceUID;
import com.bbn.protelis.processmanagement.daemon.Monitorable;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.Scenario;

public class LocalDaemon extends AbstractDaemonWrapper {
	private Daemon daemon = null;
	private Monitorable client; // needs to be configured elsewhere
	/**
	 * The testPortOffset field is used for shifting ports to avoid OS-level conflicts during rapid batch testing
	 * It is intended to be set by the JUnit test and accessed by any clients that care
	 */
	public static int testPortOffset = 0;
	
	@Override
	public void initialize(Scenario scenario) throws UnknownHostException {
		// Start the client
		client.init();
		
		// Create the daemon
		daemon = new Daemon(program, uid, client, scenario.logger);
		daemon.addListener(this);

		// Initialize the environment
		// ... from environment buttons (this also allows a scenario to tell if it's got a UI)
		for(String var : scenario.environmentButtons) {
			daemon.currentEnvironment().put(new FasterString(var), false);
		}
		// ... from JSON'ed array spec
		for(Object[] epair : environment) {
			if(epair.length!=2 || !(epair[0] instanceof String)) {
				scenario.logger.warn("Ignoring bad enviroment element: "+epair); continue;
			}
			FasterString key = new FasterString((String)epair[0]);
			Object value = epair[1];
			if(value instanceof Object[]) {
				List<Object> l = new ArrayList<>();
				for(Object v : (Object[])value) { l.add(v); }
				value = Tuple.create(l);
			}
			daemon.currentEnvironment().put(key, value);
		}

		// Run the daemon
		Thread daemonThread = new Thread(() -> daemon.run());
		daemonThread.setName("Daemon-"+alias);
		daemonThread.start();

		status = ProcessStatus.run;
	}

	@Override
	public ProcessStatus getDaemonStatus() {
		if(status==ProcessStatus.run) {
			ProcessStatus daemonStatus = daemon.getStatus();
			switch(daemonStatus) {
			case hung:
			case stop:
				status = ProcessStatus.stop;
			default:
				break;
			}
		}
		return status;
	}
	
	@Override
	public ProcessStatus getProcessStatus() {
		return client.getStatus();
	}

	@Override
	public void shutdown() {
		status = ProcessStatus.shutdown;
		daemon.stop();
		status = ProcessStatus.stop;
	}

	@Override
	public Object getValue() {
		return daemon.currentValue();
	}

	@Override
	public Map<FasterString, Object> getEnvironment() {
		return daemon.currentEnvironment();
	}

	@Override
	public void signalProcess(ProcessStatus init) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRound() {
		return daemon.getRound();
	}

	@Override
	public Set<DeviceUID> getPhysicalNeighbors() {
		return daemon.getNeighbors();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<DeviceUID> getLogicalNeighbors() {
		try {
			Tuple ln = (Tuple)daemon.currentEnvironment().get(new FasterString("logicalNeighbors"));
			return (Set)StreamSupport.stream(ln.spliterator(), false)
					.map((id) -> { return new LongDeviceUID(((Number)id).longValue()); })
					.collect(Collectors.toSet());
		} catch(Exception e) {
			return null;
		}
	}
}
