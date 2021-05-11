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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.ns2.Link;
import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;
import com.bbn.protelis.networkresourcemanagement.ns2.Node;
import com.bbn.protelis.networkresourcemanagement.ns2.Topology;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.termination.ExecutionCountTermination;
import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link NS2Parser}.
 *
 */
public class NS2ParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NS2ParserTest.class);

    /**
     * Rules for running tests.
     */
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Used by the JUnit framework")
    @Rule
    public RuleChain chain = NetworkResourceTestUtils.getStandardRuleChain();

    /**
     * Test that ns2/multinode.ns parses and check that the nodes die when they
     * should.
     * 
     * @throws IOException
     *             if there is an error reading the resource that holds the
     *             scenario
     * @throws URISyntaxException
     *             when there is an error converting the resource path for the
     *             scenario to a URI.
     */
    @Test
    public void testSimpleGraph() throws IOException, URISyntaxException {
        // pick a random port over 1024
        final Random random = new Random();
        final int port = random.nextInt(65535 - 1024) + 1024;
        LOGGER.info("Using base port " + port);

        final NodeLookupService nodeLookupService = new LocalNodeLookupService(port);

        // final String program = "true";
        // final boolean anonymous = true;
        final String program = "/protelis/com/bbn/resourcemanagement/example_resourcetracker.pt";
        final boolean anonymous = false;

        final VirtualClock clock = new SimpleClock();
        final DelegateRegionLookup regionLookupService = new DelegateRegionLookup();
        final BasicResourceManagerFactory managerFactory = new BasicResourceManagerFactory(clock);
        final BasicNetworkFactory factory = new BasicNetworkFactory(nodeLookupService, regionLookupService,
                managerFactory, program, anonymous);

        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/multinode");
        final Path baseDirectory = Paths.get(baseu.toURI());
        final Topology topology = NS2Parser.parse("multinode", baseDirectory);
        final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = new Scenario<>(topology, factory,
                DnsNameIdentifier::new);
        Assert.assertNotNull("Parse didn't create a scenario", scenario);

        regionLookupService.setDelegate(scenario);

        final long maxExecutions = 50;// 5;

        final TerminationCondition<Map<DeviceUID, NetworkServer>> condition = new ExecutionCountTermination<NetworkServer>(
                maxExecutions);
        scenario.setTerminationCondition(condition);

        final ScenarioRunner<NetworkServer, NetworkLink, NetworkClient> emulation = new ScenarioRunner<>(scenario);
        emulation.run();

        for (final Map.Entry<DeviceUID, NetworkServer> entry : scenario.getServers().entrySet()) {
            final NetworkServer node = entry.getValue();
            Assert.assertFalse("Node: " + node.getName() + " isn't dead", node.isExecuting());

            Assert.assertThat(node.getExecutionCount(), greaterThanOrEqualTo(maxExecutions));

            Assert.assertNull("Received exception inside the program loop: " + node.getExceptionThrownInProgramLoop(),
                    node.getExceptionThrownInProgramLoop());
        }

    }

    /**
     * Test that a basic network with a switch parses correctly.
     * 
     * @throws URISyntaxException
     *             if there is an error finding the test scenario directory
     * @throws IOException
     *             if there is an error reading the test files
     */
    @Test
    public void testSwitch() throws URISyntaxException, IOException {
        // dummy AP program that we aren't going to execute
        final String program = "true";
        final boolean anonymous = true;
        final NodeLookupService nodeLookupService = new LocalNodeLookupService(
                42000 /* unused */);
        final DelegateRegionLookup regionLookupService = new DelegateRegionLookup(); // unused
        final int numExpectedLinks = 4;

        final VirtualClock clock = new SimpleClock();
        final BasicResourceManagerFactory managerFactory = new BasicResourceManagerFactory(clock);
        final BasicNetworkFactory factory = new BasicNetworkFactory(nodeLookupService, regionLookupService,
                managerFactory, program, anonymous);

        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/test-switch");
        final Path baseDirectory = Paths.get(baseu.toURI());

        final Topology topology = NS2Parser.parse("test-switch", baseDirectory);
        final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = new Scenario<>(topology, factory,
                DnsNameIdentifier::new);

        Assert.assertThat(scenario.getLinks().size(), IsEqual.equalTo(numExpectedLinks));

        final String nodeAName = "nodeA";
        final NodeIdentifier nodeAId = new DnsNameIdentifier(nodeAName);
        final NetworkServer nodeA = scenario.getServers().get(nodeAId);
        Assert.assertThat(nodeA, CoreMatchers.is(IsNull.notNullValue()));

        final String nodeBName = "nodeB";
        final NodeIdentifier nodeBId = new DnsNameIdentifier(nodeBName);
        final NetworkServer nodeB = scenario.getServers().get(nodeBId);
        Assert.assertThat(nodeB, CoreMatchers.is(IsNull.notNullValue()));

        final String nodeCName = "nodeC";
        final NodeIdentifier nodeCId = new DnsNameIdentifier(nodeCName);
        final NetworkServer nodeC = scenario.getServers().get(nodeCId);
        Assert.assertThat(nodeC, CoreMatchers.is(IsNull.notNullValue()));

        final String nodeDName = "nodeD";
        final NodeIdentifier nodeDId = new DnsNameIdentifier(nodeDName);
        final NetworkServer nodeD = scenario.getServers().get(nodeDId);
        Assert.assertThat(nodeD, CoreMatchers.is(IsNull.notNullValue()));

        final Set<NodeIdentifier> nodeANeighbors = nodeA.getNeighbors();
        Assert.assertEquals(1, nodeANeighbors.size());
        Assert.assertTrue(nodeANeighbors.contains(nodeBId));

        final Set<NodeIdentifier> nodeBNeighbors = nodeB.getNeighbors();
        Assert.assertEquals(3, nodeBNeighbors.size());
        Assert.assertTrue(nodeBNeighbors.contains(nodeAId));
        Assert.assertTrue(nodeBNeighbors.contains(nodeCId));
        Assert.assertTrue(nodeBNeighbors.contains(nodeDId));

        final Set<NodeIdentifier> nodeCNeighbors = nodeC.getNeighbors();
        Assert.assertEquals(2, nodeCNeighbors.size());
        Assert.assertTrue(nodeCNeighbors.contains(nodeBId));
        Assert.assertTrue(nodeCNeighbors.contains(nodeDId));

        final Set<NodeIdentifier> nodeDNeighbors = nodeD.getNeighbors();
        Assert.assertEquals(2, nodeDNeighbors.size());
        Assert.assertTrue(nodeDNeighbors.contains(nodeBId));
        Assert.assertTrue(nodeDNeighbors.contains(nodeCId));

    }

    /**
     * Test parsing of IP address information.
     * 
     * @throws URISyntaxException
     *             if there is an error finding the test scenario directory
     * @throws IOException
     *             if there is an error reading the test files
     */
    @Test
    public void testParseIp() throws URISyntaxException, IOException {
        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/test-ip-parsing");
        final Path baseDirectory = Paths.get(baseu.toURI());

        final Topology topology = NS2Parser.parse("test-ip-parsing", baseDirectory);

        // test parsing of a single link ip setting
        final String nodeAName = "nodeA";
        final Node nodeA = getNodeFromTopology(nodeAName, topology);
        final InetAddress expectedNodeAIp = Address.getByAddress("10.0.0.1");
        final int expectedNodeALinks = 1;

        final Set<Link> nodeALinks = nodeA.getLinks();
        Assert.assertThat(nodeALinks.size(), IsEqual.equalTo(expectedNodeALinks));
        final Link nodeALink = nodeALinks.iterator().next();
        final InetAddress actualNodeAIp = nodeA.getIpAddress(nodeALink);
        Assert.assertThat(actualNodeAIp, IsEqual.equalTo(expectedNodeAIp));

        // test parsing of setting for a single link
        final String nodeCName = "nodeC";
        final Node nodeC = getNodeFromTopology(nodeCName, topology);
        final String linkCName = "link1";
        final InetAddress expectedNodeCIp = Address.getByAddress("10.1.0.1");
        final int expectedNodeCLinks = 2;

        final Set<Link> nodeCLinks = nodeC.getLinks();
        Assert.assertThat(nodeCLinks.size(), IsEqual.equalTo(expectedNodeCLinks));

        final Link nodeCLink = nodeCLinks.stream().filter(l -> linkCName.equals(l.getName())).findFirst().orElse(null);
        Assert.assertThat(nodeCLink, CoreMatchers.is(IsNull.notNullValue()));

        final InetAddress actualNodeCIp = nodeC.getIpAddress(nodeCLink);
        Assert.assertThat(actualNodeCIp, IsEqual.equalTo(expectedNodeCIp));

        // test parsing of ip lan setting
        final String nodeFName = "nodeF";
        final Node nodeF = getNodeFromTopology(nodeFName, topology);
        final InetAddress expectedNodeFIp = Address.getByAddress("10.2.0.1");
        final int expectedNodeFLinks = 1;

        final Set<Link> nodeFLinks = nodeF.getLinks();
        Assert.assertThat(nodeFLinks.size(), IsEqual.equalTo(expectedNodeFLinks));
        final Link nodeFLink = nodeFLinks.iterator().next();
        final InetAddress actualNodeFIp = nodeF.getIpAddress(nodeFLink);
        Assert.assertThat(actualNodeFIp, IsEqual.equalTo(expectedNodeFIp));

        // test parsing of ip interface setting
        final String nodeIName = "nodeI";
        final Node nodeI = getNodeFromTopology(nodeIName, topology);
        final String nodeJName = "nodeJ";
        final Node nodeJ = getNodeFromTopology(nodeJName, topology);
        final InetAddress expectedNodeIIp = Address.getByAddress("10.3.0.1");
        final int expectedNodeILinks = 1;

        final Set<Link> nodeILinks = nodeI.getLinks();
        Assert.assertThat(nodeILinks.size(), IsEqual.equalTo(expectedNodeILinks));

        final Link nodeILink = nodeILinks.stream().filter(l -> nodeJ.equals(l.getLeft()) || nodeJ.equals(l.getRight()))
                .findFirst().orElse(null);
        Assert.assertThat(nodeILink, CoreMatchers.is(IsNull.notNullValue()));

        final InetAddress actualNodeIIp = nodeI.getIpAddress(nodeILink);
        Assert.assertThat(actualNodeIIp, IsEqual.equalTo(expectedNodeIIp));

    }

    /**
     * Test that trying to set an IP on a node with multiple links without
     * specifying which link fails.
     * 
     * @throws URISyntaxException
     *             if there is an error finding the test scenario directory
     * @throws IOException
     *             if there is an error reading the test files
     */
    @Test(expected = RuntimeException.class)
    public void testFailSetIp() throws URISyntaxException, IOException {
        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/test-set-ip-fail");
        final Path baseDirectory = Paths.get(baseu.toURI());

        NS2Parser.parse("test-set-ip-fail", baseDirectory);
    }

    /**
     * Find a node in the topology and assert that it's not null.
     */
    private static Node getNodeFromTopology(final String name, final Topology topology) {
        final Node node = topology.getNodes().get(name);
        Assert.assertThat(node, CoreMatchers.is(IsNull.notNullValue()));
        return node;
    }
}
