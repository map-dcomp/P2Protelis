/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Used to build up a {@link LoadBalancerPlan} object incrementally.
 * 
 * @author jschewe
 *
 */
public class LoadBalancerPlanBuilder {

    /**
     * Create an empty plan.
     * 
     * @param region
     *            see {@link #getRegion()}
     */
    public LoadBalancerPlanBuilder(@Nonnull final RegionIdentifier region) {
        this.region = region;
    }

    /**
     * Create a plan based on the current state of a region.
     * 
     * @param prevPlan
     *            used to get the weights for the containers, may be null
     * @param regionServiceState
     *            the current state of the region
     */
    public LoadBalancerPlanBuilder(final LoadBalancerPlan prevPlan,
            @Nonnull final RegionServiceState regionServiceState) {
        this.region = prevPlan.getRegion();

        regionServiceState.getServiceReports().forEach(report -> {
            final NodeIdentifier nodeId = report.getNodeName();
            final Collection<LoadBalancerPlan.ContainerInfo> prevNodeState = prevPlan.getServicePlan()
                    .getOrDefault(nodeId, ImmutableList.of());
            final Collection<LoadBalancerPlan.ContainerInfo> nodeState = plan.computeIfAbsent(nodeId,
                    k -> new LinkedList<>());

            report.getServiceState().forEach((container, serviceState) -> {
                // don't add stopped or stopping containers to the plan as there
                // is nothing that can be done with them
                if (!ServiceState.Status.STOPPED.equals(serviceState.getStatus())
                        && !ServiceState.Status.STOPPING.equals(serviceState.getStatus())) {
                    final Optional<LoadBalancerPlan.ContainerInfo> oldInfo = prevNodeState.stream()
                            .filter(i -> container.equals(i.getId())).findAny();
                    final double weight;
                    final boolean stopTrafficTo;
                    final boolean stop;
                    if (oldInfo.isPresent()) {
                        weight = oldInfo.get().getWeight();
                        stopTrafficTo = oldInfo.get().isStopTrafficTo();
                        stop = oldInfo.get().isStop();
                    } else {
                        weight = 1;
                        stopTrafficTo = false;
                        stop = false;
                    }

                    final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(container,
                            serviceState.getService(), weight, stopTrafficTo, stop);
                    nodeState.add(newInfo);
                } // non-stopped container
            });
        });
    }

    private final RegionIdentifier region;

    /**
     * 
     * @return the region that the plan is for
     */
    @Nonnull
    public RegionIdentifier getRegion() {
        return region;
    }

    private final Map<NodeIdentifier, Collection<LoadBalancerPlan.ContainerInfo>> plan = new HashMap<>();

    /**
     * Direct access to the plan. This should only be used to read information
     * about the plan, but it can be used to modify the plan. It's advised to
     * use the other methods to modify the plan when possible.
     * 
     * @return the plan
     */
    public Map<NodeIdentifier, Collection<LoadBalancerPlan.ContainerInfo>> getPlan() {
        return plan;
    }

    /**
     * Add a new instance of the specified service container.
     * 
     * @param node
     *            the node to start the service on
     * @param service
     *            the service to start
     * @param weight
     *            the weight for the service container
     */
    public void addService(@Nonnull final NodeIdentifier node,
            @Nonnull final ServiceIdentifier<?> service,
            final double weight) {
        final LoadBalancerPlan.ContainerInfo info = new LoadBalancerPlan.ContainerInfo(null, service, weight, false,
                false);
        plan.computeIfAbsent(node, k -> new LinkedList<>()).add(info);
    }

    /**
     * Stop traffic to a container.
     * 
     * @param node
     *            the node that the container lives on
     * @param container
     *            the container
     * @throws IllegalArgumentException
     *             if the container cannot be found on the node in the current
     *             plan
     */
    public void stopTrafficToContainer(@Nonnull final NodeIdentifier node, @Nonnull final NodeIdentifier container)
            throws IllegalArgumentException {
        final LoadBalancerPlan.ContainerInfo oldInfo = findContainerInfo(node, container);
        plan.get(node).remove(oldInfo);

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(oldInfo.getId(),
                oldInfo.getService(), oldInfo.getWeight(), true, oldInfo.isStop());
        plan.get(node).add(newInfo);
    }

    /**
     * Allow traffic to a container. Inverse of
     * {@link #stopTrafficToContainer(NodeIdentifier, NodeIdentifier)}.
     * 
     * @param node
     *            the node that the container lives on
     * @param container
     *            the container
     * @throws IllegalArgumentException
     *             if the container cannot be found on the node in the current
     *             plan
     */
    public void allowTrafficToContainer(@Nonnull final NodeIdentifier node, @Nonnull final NodeIdentifier container)
            throws IllegalArgumentException {
        final LoadBalancerPlan.ContainerInfo oldInfo = findContainerInfo(node, container);
        plan.get(node).remove(oldInfo);

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(oldInfo.getId(),
                oldInfo.getService(), oldInfo.getWeight(), false, oldInfo.isStop());
        plan.get(node).add(newInfo);
    }

    /**
     * Stop the specified container.
     * 
     * @param node
     *            the node that the container lives on
     * @param container
     *            the container to stop
     * @throws IllegalArgumentException
     *             if the container cannot be found on the node in the current
     *             plan
     */
    public void stopContainer(@Nonnull final NodeIdentifier node, @Nonnull final NodeIdentifier container)
            throws IllegalArgumentException {
        final LoadBalancerPlan.ContainerInfo oldInfo = findContainerInfo(node, container);
        plan.get(node).remove(oldInfo);

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(oldInfo.getId(),
                oldInfo.getService(), oldInfo.getWeight(), oldInfo.isStopTrafficTo(), true);
        plan.get(node).add(newInfo);
    }

    /**
     * Don't stop the specified container. Inverse of
     * {@link #stopContainer(NodeIdentifier, NodeIdentifier)}.
     * 
     * @param node
     *            the node that the container lives on
     * @param container
     *            the container to stop
     * @throws IllegalArgumentException
     *             if the container cannot be found on the node in the current
     *             plan
     */
    public void unstopContainer(@Nonnull final NodeIdentifier node, @Nonnull final NodeIdentifier container)
            throws IllegalArgumentException {
        final LoadBalancerPlan.ContainerInfo oldInfo = findContainerInfo(node, container);
        plan.get(node).remove(oldInfo);

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(oldInfo.getId(),
                oldInfo.getService(), oldInfo.getWeight(), oldInfo.isStopTrafficTo(), false);
        plan.get(node).add(newInfo);
    }

    /**
     * If changing the weight to 0 or less, make sure to call
     * {@link #stopTrafficToContainer(NodeIdentifier, NodeIdentifier)} or
     * {@link #stopContainer(NodeIdentifier, NodeIdentifier)} first.
     * 
     * @param node
     *            the node to find the container on
     * @param container
     *            the container to modify
     * @param newWeight
     *            the new weight for the container
     * @throws IllegalArgumentException
     *             if the container cannot be found or the weight is less than
     *             or equal to 0 and the container is not set to stop
     */
    public void setContainerWeight(@Nonnull final NodeIdentifier node,
            @Nonnull final NodeIdentifier container,
            final double newWeight) throws IllegalArgumentException {
        final LoadBalancerPlan.ContainerInfo oldInfo = findContainerInfo(node, container);
        plan.get(node).remove(oldInfo);

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(oldInfo.getId(),
                oldInfo.getService(), newWeight, oldInfo.isStopTrafficTo(), oldInfo.isStop());
        plan.get(node).add(newInfo);
    }

    /**
     * 
     * @param regionServiceState
     *            the state of the region, used to check that all containers
     *            have been planned
     * @param overflowPlan
     *            the overflow plan to use
     * @return the plan to publish
     * @throws IllegalArgumentException
     *             if the plan is invalid (duplicate container names, missing
     *             container based on the region service state, service changed
     *             from previous)
     */
    @Nonnull
    public LoadBalancerPlan toLoadBalancerPlan(@Nonnull final RegionServiceState regionServiceState,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan)
            throws IllegalArgumentException {
        validatePlan(regionServiceState);

        final ImmutableMap.Builder<NodeIdentifier, ImmutableCollection<LoadBalancerPlan.ContainerInfo>> servicePlan = ImmutableMap
                .builder();
        plan.forEach((node, containerInfo) -> {
            servicePlan.put(node, ImmutableList.copyOf(containerInfo));
        });

        final LoadBalancerPlan newPlan = new LoadBalancerPlan(region, servicePlan.build(), overflowPlan);
        return newPlan;
    }

    private void validatePlan(@Nonnull final RegionServiceState regionServiceState) throws IllegalArgumentException {

        // check for duplicate containers
        plan.forEach((node, containers) -> {
            final Set<NodeIdentifier> seenContainers = new HashSet<>();

            containers.forEach(info -> {
                if (null != info.getId()) {
                    final boolean added = seenContainers.add(info.getId());
                    if (!added) {
                        throw new IllegalArgumentException(
                                "Two containers on node '" + node + "' have the name '" + info.getId() + "'");
                    }
                }
            });
        });

        regionServiceState.getServiceReports().forEach(sreport -> {
            final NodeIdentifier nodeName = sreport.getNodeName();

            final Collection<LoadBalancerPlan.ContainerInfo> nodeContainerInfo = plan.getOrDefault(nodeName,
                    Collections.emptyList());
            sreport.getServiceState().forEach((containerId, state) -> {
                final Optional<LoadBalancerPlan.ContainerInfo> found = nodeContainerInfo.stream()
                        .filter(info -> containerId.equals(info.getId())).findAny();

                // make sure all containers in the service state are referenced
                if (!found.isPresent()) {
                    throw new IllegalArgumentException(
                            "Container '" + containerId + "' on node '" + nodeName + "' is not referenced in the plan");
                }

                // make sure the service on a container isn't changing
                final LoadBalancerPlan.ContainerInfo info = found.get();
                if (!state.getService().equals(info.getService())) {
                    throw new IllegalArgumentException("Container '" + containerId + "' on node '" + nodeName
                            + "' is currently running service '" + state.getService()
                            + "', but the plan has it running service '" + info.getService() + "'");
                }
            });

        });

    }

    /**
     * 
     * @param node
     *            the node to find the container on
     * @param container
     *            the container to find info for
     * @return the container info found
     * @throws IllegalArgumentException
     *             if the information cannot be found
     */
    @Nonnull
    private LoadBalancerPlan.ContainerInfo findContainerInfo(@Nonnull final NodeIdentifier node,
            @Nonnull final NodeIdentifier container) throws IllegalArgumentException {
        final Optional<LoadBalancerPlan.ContainerInfo> infoFound = plan.getOrDefault(node, Collections.emptyList())
                .stream().filter(i -> container.equals(i.getId())).findAny();
        if (!infoFound.isPresent()) {
            throw new IllegalArgumentException("Cannot find container '" + container + "' on node '" + node + "'");
        }

        return infoFound.get();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " plan: " + plan + " ]";
    }

}
