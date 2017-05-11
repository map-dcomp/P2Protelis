package com.bbn.protelis.processmanagement.testbed.termination;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;

public class RoundNumberTermination implements TerminationCondition<DaemonWrapper[]> {
	private final int round;
	
	public RoundNumberTermination(int round) {
		this.round = round;
	}

	@Override
	public boolean shouldTerminate(DaemonWrapper[] daemons) {
		for(DaemonWrapper d : daemons) {
			if(d.getRound()<round) return false;
		}
		return true;
	}

}
