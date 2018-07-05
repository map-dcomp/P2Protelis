package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Allow one to plug in different resource manager implementations.
 * 
 * @param <T>
 *            the type of the n odes
 */
public interface ResourceManagerFactory<T extends NetworkServer> {

    /**
     * Create a resource manager. The node will be assigned by
     * {@link ResourceManager#init(NetworkServer, Map)}
     * 
     * @return the resource manager
     */
    @Nonnull
    ResourceManager<T> createResourceManager();

}
