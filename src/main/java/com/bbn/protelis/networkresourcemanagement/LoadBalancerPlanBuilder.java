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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Used to build up a {@link LoadBalancerPlan} object incrementally.
 * 
 * @author jschewe
 *
 */
public class LoadBalancerPlanBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerPlanBuilder.class);

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
     *            used to get the weights for the containers
     * @param resourceReports
     *            reports the current state of the region
     */
    public LoadBalancerPlanBuilder(@Nonnull final LoadBalancerPlan prevPlan,
            @Nonnull final ImmutableSet<ResourceReport> resourceReports) {
        this.region = prevPlan.getRegion();

        // need a mutable version of previous plan state
        final Map<NodeIdentifier, List<LoadBalancerPlan.ContainerInfo>> previousPlanState = new HashMap<>();
        prevPlan.getServicePlan().forEach((node, containerInfos) -> {
            previousPlanState.put(node, new LinkedList<>(containerInfos));
        });

        resourceReports.forEach(report -> {
            final NodeIdentifier nodeId = report.getNodeName();

            final List<LoadBalancerPlan.ContainerInfo> previousNodePlanState = previousPlanState.computeIfAbsent(nodeId,
                    k -> new LinkedList<>());

            final Collection<LoadBalancerPlan.ContainerInfo> nodeState = plan.computeIfAbsent(nodeId,
                    k -> new LinkedList<>());

            report.getContainerReports().forEach((container, containerReport) -> {
                // don't add stopped or stopping containers to the plan as there
                // is nothing that can be done with them
                if (!ServiceStatus.STOPPED.equals(containerReport.getServiceStatus())
                        && !ServiceStatus.STOPPING.equals(containerReport.getServiceStatus())) {

                    addToNewNodeState(nodeState, previousNodePlanState, containerReport);
                } // non-stopped container
            }); // foreach container report

            // add planned new containers that aren't running yet
            previousNodePlanState.forEach(cinfo -> {
                if (null == cinfo.getId()) {
                    nodeState.add(cinfo);
                }
            });

        }); // foreach resource report

    }

    private void addToNewNodeState(final Collection<LoadBalancerPlan.ContainerInfo> nodeState,
            final List<LoadBalancerPlan.ContainerInfo> previousNodePlanState,
            final ContainerResourceReport containerReport) {
        // check for a match by container ID
        final Optional<LoadBalancerPlan.ContainerInfo> idMatch = previousNodePlanState.stream()
                .filter(c -> Objects.equals(c.getId(), containerReport.getContainerName())).findFirst();
        if (idMatch.isPresent()) {
            final LoadBalancerPlan.ContainerInfo cinfo = idMatch.get();
            if (!containerReport.getService().equals(cinfo.getService())) {
                LOGGER.warn(
                        "Service running on container {} is {}, however the previous plan wanted {} running on this container",
                        containerReport.getContainerName(), containerReport.getService(), cinfo.getService());
            }
            nodeState.add(createNewContainerInfo(containerReport, cinfo));
            previousNodePlanState.remove(cinfo);
            return;
        }

        // check for a new container on the node with the same service
        final Optional<LoadBalancerPlan.ContainerInfo> newContainerMatch = previousNodePlanState.stream()
                .filter(c -> null == c.getId() && c.getService().equals(containerReport.getService())).findFirst();
        if (newContainerMatch.isPresent()) {
            final LoadBalancerPlan.ContainerInfo cinfo = newContainerMatch.get();
            nodeState.add(createNewContainerInfo(containerReport, cinfo));
            previousNodePlanState.remove(cinfo);
            return;
        }

        // no matches
        LOGGER.warn("Cannot find a match for container {} with service {} in {}", containerReport.getContainerName(),
                containerReport.getService(), previousNodePlanState);
        nodeState.add(createNewContainerInfo(containerReport, null));
    }

    private LoadBalancerPlan.ContainerInfo createNewContainerInfo(final ContainerResourceReport containerReport,
            final LoadBalancerPlan.ContainerInfo oldInfo) {
        final double weight;
        final boolean stopTrafficTo;
        final boolean stop;
        if (null != oldInfo) {
            weight = oldInfo.getWeight();
            stopTrafficTo = oldInfo.isStopTrafficTo();
            stop = oldInfo.isStop();
        } else {
            weight = 1;
            stopTrafficTo = false;
            stop = false;
        }

        final LoadBalancerPlan.ContainerInfo newInfo = new LoadBalancerPlan.ContainerInfo(
                containerReport.getContainerName(), containerReport.getService(), weight, stopTrafficTo, stop);
        return newInfo;
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
     * @param resourceReports
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
    public LoadBalancerPlan toLoadBalancerPlan(@Nonnull final ImmutableSet<ResourceReport> resourceReports,
            @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, Double>> overflowPlan)
            throws IllegalArgumentException {
        validatePlan(resourceReports);

        final ImmutableMap.Builder<NodeIdentifier, ImmutableCollection<LoadBalancerPlan.ContainerInfo>> servicePlan = ImmutableMap
                .builder();
        plan.forEach((node, containerInfo) -> {
            if (!containerInfo.isEmpty()) {
                servicePlan.put(node, ImmutableList.copyOf(containerInfo));
            }
        });

        final LoadBalancerPlan newPlan = new LoadBalancerPlan(region, servicePlan.build(), overflowPlan);
        return newPlan;
    }

    private void validatePlan(@Nonnull final ImmutableSet<ResourceReport> resourceReports)
            throws IllegalArgumentException {

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

        resourceReports.forEach(resourceReport -> {
            final NodeIdentifier nodeName = resourceReport.getNodeName();

            final Collection<LoadBalancerPlan.ContainerInfo> nodeContainerInfo = plan.getOrDefault(nodeName,
                    Collections.emptyList());
            resourceReport.getContainerReports().forEach((containerId, containerReport) -> {

                final Optional<LoadBalancerPlan.ContainerInfo> found = nodeContainerInfo.stream()
                        .filter(info -> containerId.equals(info.getId())).findAny();

                if (!found.isPresent()) {
                    // make sure all containers not STOPPING or STOPPED in the
                    // ServiceState are referenced
                    if (!ServiceStatus.STOPPED.equals(containerReport.getServiceStatus())
                            && !ServiceStatus.STOPPING.equals(containerReport.getServiceStatus())) {
                        throw new IllegalArgumentException("Container '" + containerId + "' on node '" + nodeName
                                + "' is not referenced in the plan");
                    }
                } else {
                    // make sure the service on a container isn't changing
                    final LoadBalancerPlan.ContainerInfo info = found.get();
                    if (!containerReport.getService().equals(info.getService())) {
                        throw new IllegalArgumentException("Container '" + containerId + "' on node '" + nodeName
                                + "' is currently running service '" + containerReport.getService()
                                + "', but the plan has it running service '" + info.getService() + "'");
                    }
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
            throw new IllegalArgumentException(
                    "Cannot find container '" + container + "' on node '" + node + "' in " + plan);
        }

        return infoFound.get();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + " plan: " + plan + " ]";
    }

}
