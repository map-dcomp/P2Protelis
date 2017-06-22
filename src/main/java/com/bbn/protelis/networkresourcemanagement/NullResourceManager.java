package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ResourceManager} that always returns an empty report and fails to do
 * anything.
 * 
 */
public class NullResourceManager implements ResourceManager {

    private final String nodeName;

    /**
     * 
     * @param nodeName the node name to use for {@link #getCurrentResourceReport()}
     */
    public NullResourceManager(final String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public ResourceReport getCurrentResourceReport() {
        return ResourceReport.getNullReport(nodeName);
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
