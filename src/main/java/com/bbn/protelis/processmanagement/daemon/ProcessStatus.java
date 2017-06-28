package com.bbn.protelis.processmanagement.daemon;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public enum ProcessStatus {
    init, 
    run, 
    compromised,
    contaminated, // affected by compromised servers, but might not itself be compromised
    shutdown, 
    hung,
    stop
}
