package com.bbn.protelis.networkresourcemanagement;

/**
 * This is the interface with the device the {@link Node} is collecting
 * information on. This interface is used to retrieve {@link ResourceReport}s
 * and to make changes to the device.
 */
public interface ResourceManager {

    /**
     * @return The current state of the device being managed. Not null.
     */
    public ResourceReport getCurrentResourceReport();

    // control methods for starting/stopping a service
    // may need migration commands here
    // Need to keep the interface generic

}
