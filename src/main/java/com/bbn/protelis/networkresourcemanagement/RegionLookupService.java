package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Used to find regions for {@link NodeIdentifier}s.
 * 
 * @author jschewe
 *
 */
public interface RegionLookupService {

    /**
     * Find the region for the specified node.
     * 
     * @param nodeId
     *            the node to lookup, may be the id of a {@link NetworkServer}
     *            or {@link NetworkClient}.
     * 
     * @return the region for the node or null if the node cannot be found
     */
    RegionIdentifier getRegionForNode(@Nonnull NodeIdentifier nodeId);

}
