package com.bbn.protelis.common.testbed.termination;

import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;

/**
 * Termination condition that never terminates. 
 * 
 * @param <T> type of object in the {@link Scenario} that will be passed here.
 */
public class NeverTerminate<T> implements TerminationCondition<T> {

    /**
     * 
     * @param ignored not used
     */
    @Override
    public boolean shouldTerminate(final T ignored) {
        return false;
    }

}
