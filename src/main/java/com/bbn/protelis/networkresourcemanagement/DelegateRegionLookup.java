/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
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

import javax.annotation.Nonnull;

/**
 * Delegate the region lookups to another lookup service. This is useful to have
 * when the delegate isn't available when the slot needs to be populated and
 * will be available before the lookup service is needed.
 * 
 * @author jschewe
 *
 */
public class DelegateRegionLookup implements RegionLookupService {

    /**
     * Upon initial construction there is no delegate so
     * {@link #getRegionForNode(NodeIdentifier) will always return null.
     * 
     * @see #setDelegate(RegionLookupService)
     */
    @Override
    @Nonnull
    public RegionIdentifier getRegionForNode(@Nonnull final NodeIdentifier nodeId) {
        if (null == delegate) {
            return RegionIdentifier.UNKNOWN;
        } else {
            return delegate.getRegionForNode(nodeId);
        }
    }

    private RegionLookupService delegate = null;

    /**
     * 
     * @return the currently stored scenario, may be null
     */
    public RegionLookupService getDelegate() {
        return delegate;
    }

    /**
     * 
     * @param delegate
     *            the delegate to use for region lookups
     * @see #getRegionForNode(NodeIdentifier)
     */
    public void setDelegate(final RegionLookupService delegate) {
        this.delegate = delegate;
    }

}
