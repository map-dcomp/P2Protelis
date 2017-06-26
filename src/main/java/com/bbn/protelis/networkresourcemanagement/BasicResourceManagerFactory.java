package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Creates {@link BasicResourceManager} objects.
 */
public class BasicResourceManagerFactory implements ResourceManagerFactory<NetworkServer> {

    @Override
    @Nonnull
    public ResourceManager createResourceManager(@Nonnull final NetworkServer node,
            @Nonnull final Map<String, Object> extraData) {
        return new BasicResourceManager(node, extraData);
    }

}
