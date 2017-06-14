package com.bbn.protelis.networkresourcemanagement.testbed;

import java.util.Map;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.networkresourcemanagement.NetworkClient;
import com.bbn.protelis.networkresourcemanagement.NetworkLink;
import com.bbn.protelis.networkresourcemanagement.NetworkServer;
import com.bbn.protelis.networkresourcemanagement.visualizer.ScenarioVisualizer;

/**
 * Class to run a {@link Scenario}.
 * 
 * @param <N>
 *            the node type
 * @param <L>
 *            the link type
 * @param <C>
 *            the client type
 */
public class ScenarioRunner<N extends NetworkServer, L extends NetworkLink, C extends NetworkClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioRunner.class);

    private final Scenario<N, L, C> scenario;
    private ScenarioVisualizer<?, ?, L, N, C> visualizer;

    /**
     * 
     * @param scenario
     *            the scenario to run
     * @param visualizer
     *            the visualizer to use, may be null. The scenario in this
     *            visualizer must match the scenario argument
     */
    public ScenarioRunner(@Nonnull final Scenario<N, L, C> scenario,
            final ScenarioVisualizer<?, ?, L, N, C> visualizer) {
        if (null != visualizer && scenario != visualizer.getScenario()) {
            throw new IllegalArgumentException("The visualizer is using a different scenario");
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initializing scenario");
        }
        this.scenario = scenario;
        this.visualizer = visualizer;
    }

    /**
     * Run a scenario: the return values live in the scenario network, which can
     * be freely polled after completion.
     */
    public void run() {
        // Initialize the daemons
        LOGGER.debug("Initializing daemons");
        for (final Map.Entry<DeviceUID, ? extends NetworkServer> entry : scenario.getServers().entrySet()) {
            entry.getValue().startExecuting();
        }

        // Launch the visualizer, if desired
        LOGGER.debug(null == visualizer ? "Launching visualizer" : "Running headless");
        if (null != visualizer) {
            visualizer.start();
        }

        LOGGER.info("Waiting while scenario runs");
        waitForTermination();

        LOGGER.info("Scenario complete");
    }

    private void waitForTermination() {
        while (true) {
            // Check if we've been told to exit by user click
            if (visualizer != null && visualizer.isClosed()) {
                LOGGER.debug("Termination signalled by user");
                break;
            }

            // Otherwise, check if the scenario has naturally terminated
            final TerminationCondition<Map<DeviceUID, N>> termination = scenario.getTerminationCondition();
            if (termination != null) {
                if (termination.shouldTerminate(scenario.getServers())) {
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
        for (final Map.Entry<DeviceUID, ? extends NetworkServer> entry : scenario.getServers().entrySet()) {
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
        for (final Map.Entry<DeviceUID, ? extends NetworkServer> entry : scenario.getServers().entrySet()) {
            if (entry.getValue().isExecuting()) {
                alldead = false;
                break;
            }
        }
        return alldead;
    }
}
