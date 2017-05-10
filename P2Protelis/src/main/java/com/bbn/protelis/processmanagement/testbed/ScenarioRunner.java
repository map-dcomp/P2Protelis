package com.bbn.protelis.processmanagement.testbed;

import java.io.IOException;

import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.visualizer.ScenarioVisualizer;


public class ScenarioRunner {
	private final Scenario scenario;
	private ScenarioVisualizer visualizer;
	
	public ScenarioRunner(Scenario scenario) {
		scenario.logger.info("Initializing scenario");
		this.scenario = scenario;
	}
	
	/**
	 * Run a scenario: the return values live in the scenario network, which can be freely polled after completion.
	 * @throws IOException 
	 */
	public void run() throws IOException {
		// Initialize the daemons
		scenario.logger.debug("Initializing daemons");
		for(DaemonWrapper d : scenario.network) { d.initialize(scenario); }
		
		// Launch the visualizer, if desired
		scenario.logger.debug(scenario.visualize?"Launching visualizer":"Running headless");
		if(scenario.visualize) { visualizer = new ScenarioVisualizer(scenario); }
		
		scenario.logger.info("Waiting while scenario runs");
		waitForTermination();
		
		scenario.logger.info("Scenario complete");
	}

	private void waitForTermination() {
		while(true) {
			// Check if we've been told to exit by user click
			if(visualizer!=null && visualizer.getProcessStatus()==ProcessStatus.stop) {
				scenario.logger.debug("Termination signalled by user");
				break;
			}
			
			// Otherwise, check if the scenario has naturally terminated
			if(scenario.termination!=null) {
				if(scenario.termination.shouldTerminate(scenario.network)) {
					scenario.logger.debug("Termination condition detected");
					break;
				}
			} else {
				if(daemonsQuiescent()) {
					scenario.logger.debug("All daemons stopped; terminating by default");
					break;
				}
			}
			// Wait for next time around
			try {
				Thread.sleep(scenario.terminationPollFrequency);
			} catch (InterruptedException e) {
				// ignore interruptions - we're just waiting in any case
			}
		}
		
		// Cleanup and exit
		scenario.logger.debug("Signalling termination to all processes");
		for(DaemonWrapper d : scenario.network) { d.shutdown(); }
        if(visualizer!=null) { visualizer.stop(); visualizer.destroy(); }
        
		scenario.logger.debug("Waiting for all daemons to stop");
		while(!daemonsQuiescent()) {
			try {
				Thread.sleep(scenario.terminationPollFrequency);
			} catch (InterruptedException e) {
				// ignore interruptions - we're just waiting in any case
			}
		}
	}
	
	private boolean daemonsQuiescent() {
		boolean alldead = true;
		for(DaemonWrapper d : scenario.network) {
			if(d.getDaemonStatus()!=ProcessStatus.stop) { alldead = false; break; }
		}
		return alldead;
	}
}
