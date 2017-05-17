package com.bbn.protelis.processmanagement.testbed.client;

import com.bbn.protelis.processmanagement.daemon.Monitorable;

/**
 * Support a rewindable message
 * @author jakebeal
 *
 */
public abstract class CrumpleZoneMonitorable extends Monitorable {
    public abstract boolean isCompromised();
    public abstract boolean isContaminated();
    
    public void rewindOneStep() {
        record.removeLast();
    }
    
    public void rewindUntilSafe() {
        while((isCompromised() || isContaminated()) && !record.isEmpty()) {
            rewindOneStep();
        }
    }
}

