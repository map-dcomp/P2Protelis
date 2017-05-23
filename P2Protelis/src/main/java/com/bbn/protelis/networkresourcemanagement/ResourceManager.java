package com.bbn.protelis.networkresourcemanagement;

/**
 * This is the interface the {@link Node} is using to collect information from.
 * This interface is used to retrieve {@link ResourceReport}s and to make
 * changes to the {@link Node}.
 */
public interface ResourceManager {

    /**
     * @return The current state of the device being managed. Not null.
     */
    ResourceReport getCurrentResourceReport();

    // control methods for starting/stopping a service
    // may need migration commands here
    // Need to keep the interface generic

}
