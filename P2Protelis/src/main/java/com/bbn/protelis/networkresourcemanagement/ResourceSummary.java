package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

/**
 * Summary state over some number of {@link Nodes} for some time.
 * 
 */
public interface ResourceSummary {

    /**
     * @return resource name -> resource value. Not null.
     * @see ResourceReport#getState()
     */
    public Map<String, Object> getState();

}
