package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.VirtualClock;

/**
 * Creates {@link BasicResourceManager} objects.
 */
public class BasicResourceManagerFactory implements ResourceManagerFactory<NetworkServer> {

    private final VirtualClock clock;

    /**
     * 
     * @param clock
     *            the clock to pass to
     *            {@link BasicResourceManager#BasicResourceManager(VirtualClock, NetworkServer, Map)}
     */
    public BasicResourceManagerFactory(@Nonnull final VirtualClock clock) {
        this.clock = clock;
    }

    @Override
    @Nonnull
    public ResourceManager createResourceManager(@Nonnull final NetworkServer node,
            @Nonnull final Map<String, Object> extraData) {
        return new BasicResourceManager(clock, node, extraData);
    }

}
