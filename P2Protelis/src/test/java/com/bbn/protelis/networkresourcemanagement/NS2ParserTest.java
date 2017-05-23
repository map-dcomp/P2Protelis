package com.bbn.protelis.networkresourcemanagement;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.protelis.lang.ProtelisLoader;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ProtelisProgram;

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

    /**
     * Test that ns2/multinode.ns parses and check that the nodes die when they
     * should.
     * 
     * @throws IOException if there is an error reading the resource that holds the scenario
     */
    @Test
    public void testSimpleGraph() throws IOException {
        final ProtelisProgram program = ProtelisLoader.parseAnonymousModule("true");

        final NodeLookupService lookupService = new LocalNodeLookupService(5000);

        final String filename = "ns2/multinode.ns";
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            Assert.assertNotNull("Couldn't find ns2 file: " + filename, stream);

            try (Reader reader = new InputStreamReader(stream)) {
                final Scenario scenario = NS2Parser.parse(filename, reader, program, lookupService);
                Assert.assertNotNull("Parse didn't create a scenario", scenario);

                final long maxExecutions = 5;

                scenario.setVisualize(false);
                scenario.setTerminationCondition(new ExecutionCountTermination(maxExecutions));

                final ScenarioRunner emulation = new ScenarioRunner(scenario);
                emulation.run();

                for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
                    final Node node = entry.getValue();
                    Assert.assertFalse("Node: " + node.getName() + " isn't dead", node.isExecuting());

                    Assert.assertThat(node.getExecutionCount(), greaterThanOrEqualTo(maxExecutions));
                }

            } // reader
        } // stream
    }

}
