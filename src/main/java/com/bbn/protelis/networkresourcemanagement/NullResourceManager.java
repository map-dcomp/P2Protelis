package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

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
    public ContainerIdentifier startService(@Nonnull final ServiceIdentifier<?> service) {
        return null;
    }

    @Override
    public boolean stopService(@Nonnull final ContainerIdentifier containerName) {
        return true;
    }

    @Override
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getServerCapacity() {
        return ImmutableMap.of();
    }

    @Override
    @Nonnull
    public ServiceReport getServiceReport() {
        return new ServiceReport(nodeId, ImmutableMap.of());
    }

}
