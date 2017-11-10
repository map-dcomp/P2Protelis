package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * This is the interface the {@link NetworkServer} is using to collect
 * information from. This interface is used to retrieve {@link ResourceReport}s
 * and to make changes to the {@link NetworkServer}.
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
     * Reserve a container.
     * 
     * @param arguments
     *            key/value pairs of arguments to pass to the container
     * @return the container that has been reserved, will be null if the
     *         container could not be reserved
     */
    ContainerIdentifier reserveContainer(@Nonnull ImmutableMap<String, String> arguments);

    /**
     * Release the container reserved with
     * {@link #reserveContainer(ImmutableMap)}.
     * 
     * @param name
     *            the value returned from reserving the container
     * @return if the release was successfully
     */
    boolean releaseContainer(@Nonnull ContainerIdentifier name);

    /**
     * Start a service in a container.
     * 
     * @param containerName
     *            the identifier for the container used with
     *            {@link #reserveContainer(ImmutableMap)}
     * @param service
     *            the service to start
     * @return if the service was started, will fail if the service is already
     *         running in this container
     */
    boolean startService(@Nonnull ContainerIdentifier containerName, @Nonnull ServiceIdentifier<?> service);

    /**
     * @param containerName
     *            the identifier for the container used with
     *            {@link #reserveContainer(ImmutableMap)}
     * @param service
     *            the service to stop
     * @return if the service was stopped, will fail if the service is not
     *         running in this container
     */
    boolean stopService(@Nonnull ContainerIdentifier containerName, @Nonnull ServiceIdentifier<?> service);

    /**
     * The capacity of the server.
     * 
     * @return attribute -> value
     */
    @Nonnull
    ImmutableMap<NodeAttribute<?>, Double> getServerCapacity();

}
