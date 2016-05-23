package com.bbn.protelis.processmanagement.testbed.client;

import java.util.Set;

import com.bbn.protelis.processmanagement.daemon.Monitorable;

public interface AttackModel {
	/**
	 * Given a client in a particular state, create a set of attacks that occur while it is in that state
	 * @param client
	 * @return Set of attacks
	 */
	Set<Attack> attackInstanceFor(Monitorable client);

}
