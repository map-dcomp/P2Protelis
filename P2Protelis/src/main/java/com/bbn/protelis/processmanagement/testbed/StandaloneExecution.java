package com.bbn.protelis.processmanagement.testbed;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.NeverTerminate;
import com.bbn.protelis.common.testbed.termination.TerminationCondition;
import com.bbn.protelis.processmanagement.testbed.daemon.AbstractDaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.termination.RoundNumberTermination;
import com.bbn.protelis.processmanagement.testbed.visualizer.DisplayNode;

public class StandaloneExecution {
	public static Logger logger;
	
	public static void main(String[] args) throws Exception {
		// when invoked directly, make sure visualization is on
		String[] extraArgs = {"-v","true"};
		runStandalone(ArrayUtils.addAll(args, extraArgs));
		System.exit(0); // need to call this because AWT thread doesn't quit otherwise
	}
	
	public static DaemonWrapper[] runStandalone(String[] args) throws Exception {
		Scenario scenario = parseArguments(args);
		
		ScenarioRunner emulation = new ScenarioRunner(scenario);
		emulation.run();
		return scenario.network;
	}

	public static Scenario parseArguments(String[] args) throws ParseException, ClassNotFoundException, IOException {
		/**
		 * Set up parsing options
		 */
		Options options = new Options();
		options.addOption("d", "--logging-level", true, "Level of logging: must be 'error', 'warn', 'info', 'debug', or 'trace'; default is 'info'");
		options.addOption("v", "--visualize", true, "Boolean for whether or not to visualize");
		options.addOption("n", "--scenario-name", true, "Name for the scenario");
		options.addOption("f", "--configuration-file", true, "Emulation network configuration file");
		options.getOption("f").setRequired(true);
		options.addOption("a", "--anonymous-program", true, "Run the argument as an anonymous Protelis class");
		options.addOption("c", "--protelis-class", true, "Run the protelis class specified in the argument");
		options.addOption("t", "--termination-condition", true, "Automatically; default only when signalled or on crash");
		options.getOption("t").setArgs(10); // potentially many arguments
		options.addOption("T", "--termination-poll-frequency", true, "Milliseconds between termination polling; default 100");
		options.addOption("e", "--environment-buttons", true, "Names of user-toggled global flags");
		options.getOption("e").setArgs(100); // potentially many arguments
		options.addOption("i", "--ignore-environment-variable", true, "Additional environment variables to ignore");
		options.getOption("i").setArgs(100); // potentially many arguments
		
		/**** Parse command line ****/
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		
		/**** Interpret parsed options ****/
		// Don't make the logger until we've checked if the level is other than default
		if(cmd.hasOption('d')) {
			String level = cmd.getOptionValue('d');
			System.getProperties().setProperty("org.slf4j.simpleLogger.defaultLogLevel", level);
		}
		logger = LoggerFactory.getLogger("StandaloneExecution");

		Scenario scenario = new Scenario(logger);
		
		if(cmd.hasOption('v')) scenario.visualize = Boolean.parseBoolean(cmd.getOptionValue('v'));
		if(cmd.hasOption('n')) scenario.scenario_name = cmd.getOptionValue('n');
		
		scenario.network = AbstractDaemonWrapper.configurationFromResource(logger,cmd.getOptionValue('f'));
		// Load Protelis program, if provided
		String defaultProgram = null;
		boolean anonymous = false;
		if(cmd.hasOption('a')) {
			if(cmd.hasOption('c'))
				throw new IllegalArgumentException("Cannot specify both a named and anonymous Protelis class to run");
			defaultProgram = cmd.getOptionValue('a');
			anonymous = true;
		} else if(cmd.hasOption('c')) {
			defaultProgram = cmd.getOptionValue('c');
			anonymous = false;
		}
		// Walk the network, substituting program where missing, parsing where provided
		for(AbstractDaemonWrapper d : (AbstractDaemonWrapper[])scenario.network) {
			if(d.program==null) { d.program = parseProgram(defaultProgram,anonymous); }
		}

		if(cmd.hasOption('t')) scenario.termination = parseTermination(cmd);
		if(cmd.hasOption('T')) scenario.terminationPollFrequency = Integer.parseUnsignedInt(cmd.getOptionValue('T'));
		if(cmd.hasOption('e')) scenario.environmentButtons = cmd.getOptionValues("e");
		if(cmd.hasOption('i')) {
			for(String i : cmd.getOptionValues('i')) { DisplayNode.ignore(i); }
		}
		
		return scenario;
	}

	private static TerminationCondition<DaemonWrapper[]> parseTermination(CommandLine cmd) {
		String[] termArgs = cmd.getOptionValues('t');
		String type = termArgs[0];
		switch(type) {
		case "never": 
			Assert.assertEquals("Terminate 'never' should not have additional arguments.",termArgs.length, 1);
			return new NeverTerminate<DaemonWrapper[]>();
		case "rounds": 
			Assert.assertEquals("'round' termination has form 'rounds [integer]'",termArgs.length, 2);
			return new RoundNumberTermination(Integer.parseUnsignedInt(termArgs[1]));
		default:
			logger.warn("Don't recognize requested termination type '"+type+"', running without termination.");
			return null;
		}
	}
	
	private static ProtelisProgram parseProgram(String program, boolean anonymous) {
		// TODO: handle anonymous appropriately
		if(anonymous) {
			return ProtelisLoader.parseAnonymousModule(program);
		} else {
			return ProtelisLoader.parse(program);
			//return ProtelisLoader.parse(ProtelisLoader.resourceFromString(program));
		}
	}
}
