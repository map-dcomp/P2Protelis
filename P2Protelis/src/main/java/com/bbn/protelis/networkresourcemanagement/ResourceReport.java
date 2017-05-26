package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Information about a node at a particular point in time.
 *
 */
public class ResourceReport {

    /**
     * Creates a report with no state.
     */
    public ResourceReport() {       
    }
    
    /**
     * @param state the state to store
     */
    public ResourceReport(@Nonnull final Map<String, Object> state) {
        this.state.putAll(state);
    }
    
    private final Map<String, Object> state = new HashMap<>();

    /**
     * The current state of the resources for a node.
     * 
     * @return resource name -> resource value. Not null. This is an unmodifiable Map.
     */
    @Nonnull
    public Map<String, Object> getState() {
        return Collections.unmodifiableMap(this.state);
    }

}
