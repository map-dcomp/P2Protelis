package com.bbn.protelis.common.testbed.termination;

public interface TerminationCondition<T> {
	public boolean shouldTerminate(T toCheck);
}
