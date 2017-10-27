package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
     * Reserve a container.
     * 
     * @param name
     *            the name of the container to reserve.
     * @param arguments
     *            key/value pairs of arguments to pass to the container
     * @return if the reserve was successful
     */
    boolean reserveContainer(@Nonnull NodeIdentifier name, @Nonnull ImmutableMap<String, String> arguments);

    /**
     * Release the container reserved with
     * {@link #reserveContainer(NodeIdentifier, ImmutableMap)}.
     * 
     * @param name
     *            the name used to reserve the container
     * @return if the release was successfully
     */
    boolean releaseContainer(@Nonnull NodeIdentifier name);

    /**
     * Start a service in a container.
     * 
     * @param containerName
     *            the identifier for the container used with
     *            {@link #reserveContainer(NodeIdentifier, ImmutableMap)}
     * @param service
     *            the service to start
     * @return if the service was started, will fail if the service is already
     *         running in this container
     */
    boolean startService(@Nonnull NodeIdentifier containerName, @Nonnull ServiceIdentifier<?> service);

    /**
     * @param containerName
     *            the identifier for the container used with
     *            {@link #reserveContainer(NodeIdentifier, ImmutableMap)}
     * @param service
     *            the service to stop
     * @return if the service was stopped, will fail if the service is not
     *         running in this container
     */
    boolean stopService(@Nonnull NodeIdentifier containerName, @Nonnull ServiceIdentifier<?> service);

    /**
     * Get the current set of services running on each container in this node.
     * 
     * @return container name -> [running services]
     */
    @Nonnull
    ImmutableMap<NodeIdentifier, ImmutableSet<ServiceIdentifier<?>>> getRunningServices();

    /**
     * The capacity of the server.
     * 
     * @return attribute -> value
     */
    @Nonnull
    ImmutableMap<NodeAttribute<?>, Double> getServerCapacity();

}
