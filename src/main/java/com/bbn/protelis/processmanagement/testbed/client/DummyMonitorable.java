package com.bbn.protelis.processmanagement.testbed.client;

import org.protelis.lang.datatype.Tuple;

import com.bbn.protelis.processmanagement.daemon.Monitorable;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.daemon.LocalDaemon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DummyMonitorable extends Monitorable {
	private ProcessStatus status = ProcessStatus.run;
	private int port = (int) (5000+Math.round(10000*Math.random()));
	private int[] dependencyList = new int[0];
	private Tuple dependencies = Tuple.create();
	private boolean firstInit = true;

	private void initializePersistentState() {
		port += LocalDaemon.testPortOffset;
		try {
			InetAddress local = InetAddress.getLocalHost();
			List<Tuple> dlist = new ArrayList<>();
			for(int d : dependencyList) {
				dlist.add(Tuple.create(local,d+LocalDaemon.testPortOffset));
			}
			dependencies = Tuple.create(dlist);
		} catch(UnknownHostException e) {
			// ignore, just end up with no dependencies
			// TODO: actually report this problem
		}
	}

	@Override
	public ProcessStatus getStatus() {
		return status;
	}

	@Override
	public Tuple knownDependencies() throws NumberFormatException,
			UnknownHostException {
		return dependencies;
	}

	@Override
	public int getCommPort() {
		return port;
	}

	@Override
	public boolean init() {
		if(firstInit) { initializePersistentState(); firstInit = false; }
		status = ProcessStatus.run;
		return true;
	}

	@Override
	public boolean shutdown() {
		status = ProcessStatus.stop;
		return true;
	}

	@Override
	public boolean crash() {
		status = ProcessStatus.hung;
		return true;
	}
}

