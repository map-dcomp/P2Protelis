package com.bbn.protelis.networkresourcemanagement;

/**
 * The interface used by a consumer of {@link ResourceSummary} information.
 */
public interface ResourceSummaryProvider {

    /**
     * Provide the consumer with the latest summary information.
     * 
     * @return the most recent summary information. Not null.
     */
    ResourceSummary getLatestState();
}
