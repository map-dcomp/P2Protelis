package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
    public boolean reserveContainer(@Nonnull final NodeIdentifier name,
            @Nonnull final ImmutableMap<String, String> arguments) {
        return true;
    }

    @Override
    public boolean releaseContainer(@Nonnull final NodeIdentifier name) {
        return true;
    }

    @Override
    public boolean startService(@Nonnull final NodeIdentifier containerName,
            @Nonnull final ServiceIdentifier<?> service) {
        return true;
    }

    @Override
    public boolean stopService(@Nonnull final NodeIdentifier containerName,
            @Nonnull final ServiceIdentifier<?> service) {
        return true;
    }

    @Override
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableSet<ServiceIdentifier<?>>> getRunningServices() {
        return ImmutableMap.of();
    }

    @Override
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getServerCapacity() {
        return ImmutableMap.of();
    }

}
