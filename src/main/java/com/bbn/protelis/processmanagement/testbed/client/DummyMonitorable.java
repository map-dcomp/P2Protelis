package com.bbn.protelis.processmanagement.testbed.client;

import org.protelis.lang.datatype.Tuple;
import org.protelis.lang.datatype.impl.ArrayTupleImpl;

import com.bbn.protelis.processmanagement.daemon.Monitorable;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.daemon.LocalDaemon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public class DummyMonitorable extends Monitorable {
    private ProcessStatus status = ProcessStatus.run;
    private int port = (int) (5000 + Math.round(10000 * Math.random()));
    private int[] dependencyList = new int[0];
    private Tuple dependencies = new ArrayTupleImpl();
    private boolean firstInit = true;

    public DummyMonitorable() {
    }
    
    public DummyMonitorable(final int port, final int[] dependencyList) {
        this.port = port;
        this.dependencyList = Arrays.copyOf(dependencyList, dependencyList.length);
    }
    
    private void initializePersistentState() {
        port += LocalDaemon.testPortOffset;
        try {
            InetAddress local = InetAddress.getLocalHost();
            Tuple[] dlist = new Tuple[dependencyList.length];
            for (int i = 0; i < dependencyList.length; i++) {
                dlist[i] = new ArrayTupleImpl(local,(Object)(dependencyList[i] + LocalDaemon.testPortOffset));
            }
            dependencies = new ArrayTupleImpl((Object[])dlist);
        } catch (UnknownHostException e) {
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
        if (firstInit) { 
            initializePersistentState();
            firstInit = false;
        }
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

