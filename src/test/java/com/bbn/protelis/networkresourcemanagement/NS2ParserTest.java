package com.bbn.protelis.networkresourcemanagement;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        final NodeLookupService lookupService = new LocalNodeLookupService(port);

        // final String program = "true";
        // final boolean anonymous = true;
        final String program = "/protelis/com/bbn/resourcemanagement/resourcetracker.pt";
        final boolean anonymous = false;

        final BasicNetworkFactory factory = new BasicNetworkFactory(lookupService, program, anonymous);

        final URL baseu = Thread.currentThread().getContextClassLoader().getResource("ns2/multinode");
        final Path baseDirectory = Paths.get(baseu.toURI());
        final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = NS2Parser.parse("multinode", baseDirectory,
                factory);
        Assert.assertNotNull("Parse didn't create a scenario", scenario);

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
        }

    }

}
