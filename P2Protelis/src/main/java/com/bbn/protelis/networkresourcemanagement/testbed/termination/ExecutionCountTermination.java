package com.bbn.protelis.networkresourcemanagement.testbed.termination;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * Terminate after a number of executions of a set of {@link Node}s.
 */
public class ExecutionCountTermination implements TerminationCondition<Map<DeviceUID, Node>> {
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
    public boolean shouldTerminate(final Map<DeviceUID, Node> nodes) {
        for (final Map.Entry<DeviceUID, Node> entry : nodes.entrySet()) {
            if (entry.getValue().getExecutionCount() < round) {
                return false;
            }
        }
        return true;
    }

}
