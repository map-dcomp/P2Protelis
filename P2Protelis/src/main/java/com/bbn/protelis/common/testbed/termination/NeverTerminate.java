package com.bbn.protelis.common.testbed.termination;

public class NeverTerminate<T> implements TerminationCondition<T> {

    @Override
    public boolean shouldTerminate(T ignored) {
        return false;
    }

}
