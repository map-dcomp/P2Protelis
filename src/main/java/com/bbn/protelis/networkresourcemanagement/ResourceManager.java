package com.bbn.protelis.networkresourcemanagement;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This is the interface the {@link NetworkServer} is using to collect
 * information from. This interface is used to retrieve {@link ResourceReport}s
 * and to make changes to the {@link NetworkServer}.
 */
public interface ResourceManager {

    /**
     * @return The current state of the device being managed. Not null.
     */
    @Nonnull
    ResourceReport getCurrentResourceReport();

    /**
     * Reserve a container.
     * 
     * @param name
     *            the name of the container to reserve.
     * @param arguments
     *            key/value pairs of arguments to pass to the container
     * @return if the reserve was successful
     */
    boolean reserveContainer(@Nonnull String name, @Nonnull Map<String, String> arguments);

    /**
     * Release the container reserved with
     * {@link #reserveContainer(String, Map)}.
     * 
     * @param name
     *            the name used to reserve the container
     * @return if the release was successfully
     */
    boolean releaseContainer(@Nonnull String name);

    /**
     * Start a task in a container.
     * 
     * @param containerName
     *            the container to start in, used with
     *            {@link #reserveContainer(String, Map)}
     * @param taskName
     *            the name of the task to start
     * @param arguments
     *            the arguments for the task
     * @param environment
     *            the environment for the task
     * @return if the start was successfull
     */
    boolean startTask(@Nonnull String containerName,
            @Nonnull String taskName,
            @Nonnull ImmutableList<String> arguments,
            @Nonnull ImmutableMap<String, String> environment);

    /**
     * Stop a task.
     * 
     * @param containerName
     *            the container that the task is running in
     * @param taskName
     *            the name of the task from
     *            {@link #startTask(String, String, List, Map)}
     * @return if the stop was successfull
     */
    boolean stopTask(@Nonnull String containerName, @Nonnull String taskName);

}
