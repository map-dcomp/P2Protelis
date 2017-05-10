package com.bbn.protelis.processmanagement.testbed.termination;

import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;

public class NeverTerminate implements TerminationCondition {

	@Override
	public boolean shouldTerminate(DaemonWrapper[] daemons) {
		return false;
	}

}
