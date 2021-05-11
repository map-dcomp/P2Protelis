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

import java.util.Map;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;

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

    /**
     * 
     * @param scenario
     *            the scenario to run
     */
    public ScenarioRunner(@Nonnull final Scenario<N, L, C> scenario) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initializing scenario");
        }
        this.scenario = scenario;
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

        LOGGER.info("Waiting while scenario runs");
        waitForTermination();

        LOGGER.info("Scenario complete");
    }

    private void waitForTermination() {
        while (true) {

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
