package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * The interface used by a consumer of {@link ResourceSummary} information.
 */
public interface ResourceSummaryProvider {

    /**
     * Provide the consumer with the latest summary information.
     * 
     * @return the most recent summary information. Not null.
     */
    @Nonnull
    ResourceSummary getResourceSummary();
}
