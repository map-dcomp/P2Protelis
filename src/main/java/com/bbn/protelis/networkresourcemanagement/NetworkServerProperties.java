/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
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
