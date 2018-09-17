package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Parse functions for use with
 * {@link NetworkServer#processExtraData(java.util.Map)}.
 * 
 * @author jschewe
 *
 */
public final class NetworkServerProperties {

    private NetworkServerProperties() {
    }

    /**
     * The key into extra data passed to {@link #processExtraData(Map)} that
     * specifies the region for a node. This will create a
     * {@link StringRegionIdentifier}.
     */
    public static final String EXTRA_DATA_REGION_KEY = "region";

    /**
     * @param extraData
     *            the data to process
     * @return the region or null if there is no region specified
     */
    public static String parseRegionName(@Nonnull final Map<String, Object> extraData) {
        final Object regionValue = extraData.get(EXTRA_DATA_REGION_KEY);
        if (null != regionValue) {
            final String regionName = regionValue.toString();
            return regionName;
        } else {
            return null;
        }
    }

}
