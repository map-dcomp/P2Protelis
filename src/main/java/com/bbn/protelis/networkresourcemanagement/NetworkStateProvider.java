package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Allow one to get the {@link NetworkState}.
 */
public interface NetworkStateProvider {

    /**
     * Current information about the state of the network.
     * 
     * @return the network state
     */
    @Nonnull
    NetworkState getNetworkState();

}
