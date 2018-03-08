package com.bbn.protelis.networkresourcemanagement;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;
import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;
import com.bbn.protelis.networkresourcemanagement.testbed.ScenarioRunner;
import com.bbn.protelis.networkresourcemanagement.testbed.termination.ExecutionCountTermination;

/**
 * Tests for {@link NS2Parser}.
 *
 */
public class NS2ParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NS2ParserTest.class);

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
        final String program = "/protelis/com/bbn/resourcemanagement/resourcetracker.pt";
        final boolean anonymous = false;

        final DelegateRegionLookup regionLookupService = new DelegateRegionLookup();
        final BasicResourceManagerFactory managerFactory = new BasicResourceManagerFactory();
        final BasicNetworkFactory factory = new BasicNetworkFactory(nodeLookupService, regionLookupService,
                managerFactory, program, anonymous);

        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/multinode");
        final Path baseDirectory = Paths.get(baseu.toURI());
        final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = NS2Parser.parse("multinode", baseDirectory,
                factory);
        Assert.assertNotNull("Parse didn't create a scenario", scenario);

        regionLookupService.setDelegate(scenario);

        final long maxExecutions = 50;// 5;

        final TerminationCondition<Map<DeviceUID, NetworkServer>> condition = new ExecutionCountTermination<NetworkServer>(
                maxExecutions);
        scenario.setTerminationCondition(condition);

        final ScenarioRunner<NetworkServer, NetworkLink, NetworkClient> emulation = new ScenarioRunner<>(scenario,
                null);
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
        final NodeLookupService nodeLookupService = new LocalNodeLookupService(42000 /* unused */);
        final DelegateRegionLookup regionLookupService = new DelegateRegionLookup(); // unused

        final BasicResourceManagerFactory managerFactory = new BasicResourceManagerFactory();
        final BasicNetworkFactory factory = new BasicNetworkFactory(nodeLookupService, regionLookupService, managerFactory,
                program, anonymous);

        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/test-switch");
        final Path baseDirectory = Paths.get(baseu.toURI());

        final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = NS2Parser.parse("test-switch",
                baseDirectory, factory);

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

}
