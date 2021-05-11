package com.bbn.protelis.common.testbed.termination;

/**
 * Interface for defining when a scenario should terminate.
 * 
 * @param <T>
 *            type of object in the scenario that will be passed here.
 */
public interface TerminationCondition<T> {
    /**
     * Check if the scenario should terminate. This is called periodically to
     * check if the scenario should exit.
     * 
     * @param toCheck
     *            the state to check
     * @return true if the scenario should terminate.
     */
    boolean shouldTerminate(T toCheck);
}
