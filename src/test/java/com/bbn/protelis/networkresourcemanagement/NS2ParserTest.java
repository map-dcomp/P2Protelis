package com.bbn.protelis.networkresourcemanagement;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

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
     */
    @Test
    public void testSimpleGraph() throws IOException {
        // pick a random port over 1024
        final Random random = new Random();
        final int port = random.nextInt(65535 - 1024) + 1024;
        LOGGER.info("Using base port " + port);

        final NodeLookupService lookupService = new LocalNodeLookupService(port);

        final String filename = "ns2/multinode.ns";
        // String program = "true";
        // boolean anonymous = true;
        String program = "/protelis/com/bbn/resourcemanagement/resourcetracker.pt";
        boolean anonymous = false;

        final BasicNetworkFactory factory = new BasicNetworkFactory(lookupService, program, anonymous);
        final Scenario<Node, Link> scenario = NS2Parser.parseFromResource(filename, "ns2/multinode", factory);
        Assert.assertNotNull("Parse didn't create a scenario", scenario);

        final long maxExecutions = 5;

        final TerminationCondition<Map<DeviceUID, Node>> condition = new ExecutionCountTermination<Node>(maxExecutions);
        scenario.setTerminationCondition(condition);

        final ScenarioRunner<Node, Link> emulation = new ScenarioRunner<>(scenario, null);
        emulation.run();

        for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
            final Node node = entry.getValue();
            Assert.assertFalse("Node: " + node.getName() + " isn't dead", node.isExecuting());

            Assert.assertThat(node.getExecutionCount(), greaterThanOrEqualTo(maxExecutions));
        }

    }

}
