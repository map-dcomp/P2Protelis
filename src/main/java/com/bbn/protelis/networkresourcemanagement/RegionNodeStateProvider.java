package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Provide information about the nodes in a region.
 * 
 */
public interface RegionNodeStateProvider {

    /**
     * 
     * @return the detailed state of the nodes in a region
     */
    @Nonnull
    RegionNodeState getRegionNodeState();

}
