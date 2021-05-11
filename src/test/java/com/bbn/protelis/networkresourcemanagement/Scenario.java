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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.ns2.NetworkDevice;
import com.bbn.protelis.networkresourcemanagement.ns2.Node;
import com.bbn.protelis.networkresourcemanagement.ns2.Switch;
import com.bbn.protelis.networkresourcemanagement.ns2.Topology;

/**
 * A test scenario.
 * 
 * @param <N>
 *            the node type
 * @param <L>
 *            the link type
 * @param <C>
 *            the client type
 */
public class Scenario<N extends NetworkServer, L extends NetworkLink, C extends NetworkClient>
        implements RegionLookupService {

    private TerminationCondition<Map<DeviceUID, N>> terminationCondition;

    /**
     * @return may be null
     */
    public final TerminationCondition<Map<DeviceUID, N>> getTerminationCondition() {
        return terminationCondition;
    }

    private long terminationPollFrequency = 1 * 1000;

    /**
     * @return number of milliseconds between checks for termination
     */
    public final long getTerminationPollFrequency() {
        return terminationPollFrequency;
    }

    /**
     * @param v
     *            milliseconds between checks for termination
     */
    public final void setTerminationPollFrequency(final long v) {
        terminationPollFrequency = v;
    }

    /**
     * Condition for exiting the scenario.
     * 
     * @param v
     *            the new value, may be null
     */
    public final void setTerminationCondition(final TerminationCondition<Map<DeviceUID, N>> v) {
        terminationCondition = v;
    }

    /**
     * Name of scenario, to put into log files and visualization window name.
     */
    private final String name;

    /**
     * @return name of the scenario
     */
    public String getName() {
        return name;
    }

    /**
     * Servers in the network to run, including Protelis program for each
     * device.
     * 
     * @return unmodifiable map of the nodes, key is the UID
     */
    public Map<DeviceUID, N> getServers() {
        return Collections.unmodifiableMap(this.servers);
    }

    private final Map<DeviceUID, N> servers = new HashMap<>();

    /**
     * Clients in the network to run.
     * 
     * @return unmodifiable map of the clients, key is the UID
     */
    public Map<DeviceUID, C> getClients() {
        return Collections.unmodifiableMap(this.clients);
    }

    private final Map<DeviceUID, C> clients = new HashMap<>();

    /**
     * Links in the network.
     * 
     * @return unmodifiable list of the links
     */
    public Set<L> getLinks() {
        return Collections.unmodifiableSet(this.links);
    }

    // RegionLookupService
    @Override
    @Nonnull
    public RegionIdentifier getRegionForNode(@Nonnull final NodeIdentifier nodeId) {
        final N server = servers.get(nodeId);
        if (null != server) {
            return server.getRegionIdentifier();
        } else {
            final C client = clients.get(nodeId);
            if (null != client) {
                return client.getRegionIdentifier();
            } else {
                return RegionIdentifier.UNKNOWN;
            }
        }
    }
    // end RegionLookupService

    private final Set<L> links = new HashSet<>();

    /**
     * 
     * @param name
     *            the name of the scenario
     * @param servers
     *            the servers in the scenario
     * @param links
     *            the links in the scenario
     * @param clients
     *            the clients in the scenario
     */
    public Scenario(final String name,
            final Collection<N> servers,
            final Collection<L> links,
            final Collection<C> clients) {
        this.name = name;
        servers.forEach(s -> {
            this.servers.put(s.getNodeIdentifier(), s);
        });
        clients.forEach(c -> {
            this.clients.put(c.getNodeIdentifier(), c);
        });
        links.forEach(l -> {
            this.links.add(l);
        });
    }

    /**
     * Create a scenario from the toplogy.
     * 
     * @param topology
     *            the toplogy to read
     * @param factory
     *            used to create the network objects
     * @param createNodeIdentifier
     *            function to map string names to NodeIdentifiers
     */
    public Scenario(final Topology topology,
            final NetworkFactory<N, L, C> factory,
            final Function<String, NodeIdentifier> createNodeIdentifier) {
        this.name = topology.getName();

        // create all of the nodes
        final Map<String, NetworkNode> nameToNetworkNode = new HashMap<>();
        topology.getNodes().forEach((nodeName, node) -> {
            final NodeIdentifier id = createNodeIdentifier.apply(nodeName);

            final NetworkNode netNode;
            if (node.isClient()) {
                final C c = factory.createClient(id, node.getExtraData());
                this.clients.put(id, c);
                netNode = c;
            } else {
                final N s = factory.createServer(id, node.getExtraData());
                this.servers.put(id, s);
                s.setHardware(node.getHardware());

                netNode = s;
            }

            nameToNetworkNode.put(nodeName, netNode);
        });

        // create all of the links
        topology.getNodes().forEach((nodeName, node) -> {
            node.getLinks().forEach(l -> {
                final NetworkDevice leftDev = l.getLeft();
                final NetworkDevice rightDev = l.getRight();

                final Set<Node> leftNodes = new HashSet<>();
                if (leftDev instanceof Switch) {
                    leftNodes.addAll(((Switch) leftDev).getNodes().stream().filter(n -> !n.equals(rightDev))
                            .collect(Collectors.toSet()));
                } else if (leftDev instanceof Node) {
                    leftNodes.add((Node) leftDev);
                } else {
                    throw new RuntimeException("Unexpectd NetworkDevice type: " + leftDev);
                }

                final Set<Node> rightNodes = new HashSet<>();
                if (rightDev instanceof Switch) {
                    rightNodes.addAll(((Switch) rightDev).getNodes().stream().filter(n -> !n.equals(leftDev))
                            .collect(Collectors.toSet()));
                } else if (rightDev instanceof Node) {
                    rightNodes.add((Node) rightDev);
                } else {
                    throw new RuntimeException("Unexpectd NetworkDevice type: " + rightDev);
                }

                leftNodes.forEach(leftNode -> {
                    final NetworkNode leftNetNode = nameToNetworkNode.get(leftNode.getName());

                    rightNodes.forEach(rightNode -> {
                        final NetworkNode rightNetNode = nameToNetworkNode.get(rightNode.getName());

                        final L netLink = factory.createLink(l.getName(), leftNetNode, rightNetNode, l.getBandwidth(),
                                l.getDelay());
                        this.links.add(netLink);

                        leftNetNode.addNeighbor(rightNetNode, netLink.getBandwidth());
                        rightNetNode.addNeighbor(leftNetNode, netLink.getBandwidth());
                    });
                });

            });
        });

    }
}
