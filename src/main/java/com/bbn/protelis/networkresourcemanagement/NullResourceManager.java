package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ResourceManager} that always returns an empty report and fails to do
 * anything.
 * 
 */
public class NullResourceManager implements ResourceManager {

    private final NodeIdentifier nodeId;
    private final VirtualClock clock = new SimpleClock();

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
    public ContainerIdentifier startService(@Nonnull final ServiceIdentifier<?> service,
            @Nonnull final ContainerParameters parmeters) {
        return null;
    }

    @Override
    public boolean stopService(@Nonnull final ContainerIdentifier containerName) {
        return true;
    }

    @Override
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getComputeCapacity() {
        return ImmutableMap.of();
    }

    @Override
    @Nonnull
    public ServiceReport getServiceReport() {
        return new ServiceReport(nodeId, ImmutableMap.of());
    }

    @Override
    @Nonnull
    public VirtualClock getClock() {
        return clock;
    }
}
