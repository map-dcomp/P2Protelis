package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

/**
 * Information about a device at a particular point in time.
 *
 */
public interface ResourceReport {

    /**
     * The current state of the resources for a device.
     * 
     * @return resource name -> resource value. Not null.
     */
    Map<String, Object> getState();

}
