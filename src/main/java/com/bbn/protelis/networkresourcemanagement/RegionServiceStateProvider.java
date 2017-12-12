package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Provide information about the nodes in a region.
 * 
 */
public interface RegionServiceStateProvider {

    /**
     * 
     * @return the detailed state of the services in a region
     */
    @Nonnull
    RegionServiceState getRegionServiceState();

}
