package com.bbn.protelis.networkresourcemanagement.testbed.termination;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * Terminate after a number of executions of a set of {@link Node}s.
 * 
 * @param <N> the node type to deal with
 */
public class ExecutionCountTermination<N extends Node> implements TerminationCondition<Map<DeviceUID, N>> {
    private final long round;

    /**
     * 
     * @param round
     *            how many executions to terminate after
     */
    public ExecutionCountTermination(final long round) {
        this.round = round;
    }

    @Override
    public boolean shouldTerminate(final Map<DeviceUID, N> nodes) {
        for (final Map.Entry<DeviceUID, N> entry : nodes.entrySet()) {
            if (entry.getValue().getExecutionCount() < round) {
                return false;
            }
        }
        return true;
    }

}
