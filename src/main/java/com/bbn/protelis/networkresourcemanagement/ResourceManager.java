/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.VirtualClock;
import com.google.common.collect.ImmutableMap;

/**
 * This is the interface the {@link NetworkServer} is using to collect
 * information from. This interface is used to retrieve {@link ResourceReport}s
 * and to make changes to the {@link NetworkServer}.
 * 
 * This interface assumes that each containern runs only a single service.
 * 
 * @param <T>
 *            the type of {@link NetworkServer} that is being managed.
 */
public interface ResourceManager<T extends NetworkServer> {

    /**
     * Initialize the object with the node that it will manage.
     * 
     * @param node
     *            the node to be managed
     * @param extraData
     *            extra information about the node, usually used to set some
     *            extra properties
     */
    void init(@Nonnull T node, @Nonnull Map<String, Object> extraData);

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
    NodeIdentifier startService(@Nonnull ServiceIdentifier<?> service, @Nonnull ContainerParameters parameters);

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
    boolean stopService(@Nonnull NodeIdentifier containerName);

    /**
     * The capacity of the server.
     * 
     * @return attribute -> value
     */
    @Nonnull
    ImmutableMap<NodeAttribute, Double> getComputeCapacity();

    /**
     * @return the clock used by the resource manager for timing
     */
    @Nonnull
    VirtualClock getClock();

    /**
     * Fetch the image for the specified service. If the image is already local
     * this is does nothing. This method is asynchronous, it will request the
     * image and return.
     * 
     * @param service
     *            the service to find the image for
     */
    void fetchImage(@Nonnull ServiceIdentifier<?> service);

    /**
     * Wait for the image for the specified service to arrive on the current
     * node. If the service doesn't specify an image, this method will return
     * immediately.
     * 
     * @param service
     *            the service specifying the image
     * @return if the image is local when the method returns
     */
    boolean waitForImage(@Nonnull ServiceIdentifier<?> service);

    /**
     * Record a failed request. This information can be used to enhance the
     * demand computation.
     * 
     * @param serverEndTime
     *            the expected end time of the server load had this request
     *            succeeded
     * @param serverLoad
     *            the expected server load had this request succeeded
     * @param networkEndTime
     *            the expected end time of the network load had this request
     *            succeeded
     * @param networkLoad
     *            the expected network load had this request succeeded
     * @param containerId
     *            the container that failed to process the request
     * @param client
     *            the client executing the request
     */
    void addFailedRequest(NodeIdentifier client,
            NodeIdentifier containerId,
            long serverEndTime,
            Map<NodeAttribute, Double> serverLoad,
            long networkEndTime,
            Map<LinkAttribute, Double> networkLoad);

}
