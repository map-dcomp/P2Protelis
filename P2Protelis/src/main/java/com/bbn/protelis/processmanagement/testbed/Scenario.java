package com.bbn.protelis.processmanagement.testbed;

import org.slf4j.Logger;

import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;

public class Scenario {
    /**
     * Where should logging messages be directed?
     * Should be set by the parent process that creates the scenario.
     */
    public final Logger logger;
    
    /**
     * Should the system run with visualization, or headless? 
     */
    public boolean visualize = true;
    /**
     * Name of scenario, to put into log files and visualization window name
     */
    public String scenario_name = "Unnamed Scenario";

    /**
     * Structure of the network to run, including Protelis program for each device
     */
    public DaemonWrapper[] network = new DaemonWrapper[0];

    /**
     * Set of boolean buttons to be toggled in the visualizer
     */
    public String[] environmentButtons = new String[0];

    /**
     * Determines when a scenario should automatically terminate.
     * If null, then there is no automatic termination.
     */
    public TerminationCondition<DaemonWrapper[]> termination = null;
    
    /**
     * Milliseconds between checks for termination
     */
    public long terminationPollFrequency = 100;
    
    /**
     * Constructor for creating a scenario with default conditions
     * @param logger
     */
    public Scenario(Logger logger) {
        this.logger = logger;
    }

}
