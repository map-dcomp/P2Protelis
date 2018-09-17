package com.bbn.protelis.processmanagement.testbed.daemon;

import java.io.IOException;
import java.util.Set;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ExecutionEnvironment;

import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.Scenario;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public interface DaemonWrapper {
    /**
     * Every DaemonWrapper should be instantiable with a no-argument constructor from a JSON object; initialize then move from that initial state into an live, executing daemon.
     * @throws IOException 
     */
    void initialize(Scenario scenario) throws IOException;
    
    /**
     * @return Status of the daemon
     */
    ProcessStatus getDaemonStatus();
    /**
     * @return Status of the process being managed by the daemon
     */
    ProcessStatus getProcessStatus();
    
    /**
     * Calling shutdown signals the wrapped daemon to stop executing and shut down.
     */
    void shutdown();
    
    Object getValue();
    int getRound();
    ExecutionEnvironment getEnvironment();
    long getUID();
    void signalProcess(ProcessStatus init);
    Set<DeviceUID> getPhysicalNeighbors();
    Set<DeviceUID> getLogicalNeighbors();
}
