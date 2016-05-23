package com.bbn.protelis.processmanagement.daemon;

public enum ProcessStatus {
	init, 
	run, 
	compromised,
	contaminated, // affected by compromised servers, but might not itself be compromised
	shutdown, 
	hung,
	stop
}
