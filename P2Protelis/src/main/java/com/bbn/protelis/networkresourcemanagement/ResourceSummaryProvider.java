package com.bbn.protelis.networkresourcemanagement;

/**
 * The interface used by a consumer of {@link ResourceSummary} information.
 */
public interface ResourceSummaryProvider {

	/**
	 * Provide the consumer with the latest summary information.
	 * 
	 * @param summary
	 *            the most recent summary information. Not null.
	 */
	public ResourceSummary getLatestState();
}
