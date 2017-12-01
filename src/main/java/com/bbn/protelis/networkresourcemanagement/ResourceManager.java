package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * This is the interface the {@link NetworkServer} is using to collect
 * information from. This interface is used to retrieve {@link ResourceReport}s
 * and to make changes to the {@link NetworkServer}.
 * 
 * This interface assumes that each containern runs only a single service.
 */
public interface ResourceManager {

    /**
     * @return The current state of the device being managed. Not null.
     * @param demandWindow
     *            the window size that demand should be estimated over
     */
    @Nonnull
    ResourceReport getCurrentResourceReport(@Nonnull ResourceReport.EstimationWindow demandWindow);

    /**
     * @return information about the services running on the node
     */
    @Nonnull
    ServiceReport getServiceReport();

    /**
     * Start a service in a container.
     * 
     * @param service
     *            the service to start
     * @return the identifier of the container that the service was started in,
     *         null if there was an error finding a container or starting the
     *         service
     * @param parameters
     *            the parameters for starting the service container
     * @see ContainerResourceReport
     */
    ContainerIdentifier startService(@Nonnull ServiceIdentifier<?> service, @Nonnull ContainerParameters parameters);

    /**
     * Stop the service running in the specified container. This method will
     * return immediately after telling the service to stop. Once the service
     * has shutdown the container will be cleaned up by the implementation.
     * 
     * @param containerName
     *            the identifier for the container used with
     *            {@link #startService(ServiceIdentifier)}
     * @return if the service was able to be notified to stop
     */
    boolean stopService(@Nonnull ContainerIdentifier containerName);

    /**
     * The capacity of the server.
     * 
     * @return attribute -> value
     */
    @Nonnull
    ImmutableMap<NodeAttribute<?>, Double> getServerCapacity();

}
