package com.bbn.protelis.common.testbed.termination;

/**
 * Termination condition that never terminates.
 * 
 * @param <T>
 *            type of object in the scenario that will be passed here.
 */
public class NeverTerminate<T> implements TerminationCondition<T> {

    /**
     * 
     * @param ignored
     *            not used
     */
    @Override
    public boolean shouldTerminate(final T ignored) {
        return false;
    }

}
