package com.bbn.protelis.networkresourcemanagement.testbed;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.Node;
import com.bbn.protelis.networkresourcemanagement.visualizer.ScenarioVisualizer;

/**
 * Class to run a {@link Scenario}.
 */
public class ScenarioRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioRunner.class);

    private final Scenario scenario;
    private ScenarioVisualizer visualizer;

    /**
     * 
     * @param scenario
     *            the scenario to run
     */
    public ScenarioRunner(final Scenario scenario) {
        LOGGER.info("Initializing scenario");
        this.scenario = scenario;
    }

    /**
     * Run a scenario: the return values live in the scenario network, which can
     * be freely polled after completion.
     */
    public void run() {
        // Initialize the daemons
        LOGGER.debug("Initializing daemons");
        for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
            entry.getValue().startExecuting();
        }

        // Launch the visualizer, if desired
        LOGGER.debug(scenario.getVisualize() ? "Launching visualizer" : "Running headless");
        if (scenario.getVisualize()) {
            visualizer = new ScenarioVisualizer(scenario);
        }

        LOGGER.info("Waiting while scenario runs");
        waitForTermination();

        LOGGER.info("Scenario complete");
    }

    private void waitForTermination() {
        while (true) {
            // Check if we've been told to exit by user click
            if (visualizer != null && visualizer.isVisible()) {
                LOGGER.debug("Termination signalled by user");
                break;
            }

            // Otherwise, check if the scenario has naturally terminated
            final TerminationCondition<Map<DeviceUID, Node>> termination = scenario.getTerminationCondition();
            if (termination != null) {
                if (termination.shouldTerminate(scenario.getNodes())) {
                    LOGGER.debug("Termination condition detected");
                    break;
                }
            } else {
                if (daemonsQuiescent()) {
                    LOGGER.debug("All daemons stopped; terminating by default");
                    break;
                }
            }
            // Wait for next time around
            try {
                Thread.sleep(scenario.getTerminationPollFrequency());
            } catch (InterruptedException e) {
                // ignore interruptions - we're just waiting in any case
            }
        }

        // Cleanup and exit
        LOGGER.debug("Signalling termination to all processes");
        for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
            entry.getValue().stopExecuting();
        }
        if (visualizer != null) {
            visualizer.stop();
            visualizer.destroy();
        }

        LOGGER.debug("Waiting for all daemons to stop");
        while (!daemonsQuiescent()) {
            try {
                Thread.sleep(scenario.getTerminationPollFrequency());
            } catch (InterruptedException e) {
                // ignore interruptions - we're just waiting in any case
            }
        }
    }

    private boolean daemonsQuiescent() {
        boolean alldead = true;
        for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
            if (entry.getValue().isExecuting()) {
                alldead = false;
                break;
            }
        }
        return alldead;
    }
}
