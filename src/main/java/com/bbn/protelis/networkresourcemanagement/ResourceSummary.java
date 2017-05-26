package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Summary state over some number of {@link Nodes} for some time.
 */
public class ResourceSummary {

    private final Map<String, Object> state = new HashMap<>();

    /**
     * Construct an empty summary.
     */
    public ResourceSummary() {
        
    }
    
    /**
     * 
     * @param state the state for the summary
     */
    public ResourceSummary(@Nonnull final Map<String, Object> state) {
        this.state.putAll(state);
    }

    /**
     * @return resource name -> resource value. Not null. Not modifiable.
     * @see ResourceReport#getState()
     */
    @Nonnull
    public Map<String, Object> getState() {
        return Collections.unmodifiableMap(this.state);
    }

}
