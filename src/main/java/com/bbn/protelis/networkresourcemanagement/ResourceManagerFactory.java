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
     * Create a resource manager.
     * 
     * @param node
     *            the node that the manager is for
     * @param extraData
     *            extra data read in when creating the node, used for testing
     * @return the resource manager to use
     */
    @Nonnull
    ResourceManager createResourceManager(@Nonnull T node, @Nonnull Map<String, Object> extraData);

}
