package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;

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
     * @see #setDelegate(Scenario)
     */
    @Override
    public RegionIdentifier getRegionForNode(@Nonnull final NodeIdentifier nodeId) {
        if (null == delegate) {
            return null;
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
