package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ResourceManager} that always returns an empty report and fails to do
 * anything.
 * 
 */
public class NullResourceManager implements ResourceManager {

    private final NodeIdentifier nodeId;

    /**
     * 
     * @param nodeName
     *            the node name to use for {@link #getCurrentResourceReport()}
     */
    public NullResourceManager(final NodeIdentifier nodeName) {
        this.nodeId = nodeName;
    }

    @Override
    public ResourceReport getCurrentResourceReport(@Nonnull final ResourceReport.EstimationWindow demandWindow) {
        return ResourceReport.getNullReport(nodeId, demandWindow);
    }

    @Override
    public boolean reserveContainer(final String name, final Map<String, String> arguments) {
        return false;
    }

    @Override
    public boolean releaseContainer(final String name) {
        return false;
    }

    @Override
    public boolean startTask(final String containerName,
            final String taskName,
            final ImmutableList<String> arguments,
            final ImmutableMap<String, String> environment) {
        return false;
    }

    @Override
    public boolean stopTask(final String containerName, final String taskName) {
        return false;
    }

}
