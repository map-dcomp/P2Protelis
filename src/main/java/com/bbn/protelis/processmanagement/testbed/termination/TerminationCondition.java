package com.bbn.protelis.processmanagement.testbed.termination;

import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;

public interface TerminationCondition {
	public boolean shouldTerminate(DaemonWrapper daemons[]);
}
