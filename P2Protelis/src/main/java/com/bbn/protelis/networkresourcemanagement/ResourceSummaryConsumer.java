package com.bbn.protelis.networkresourcemanagement;

/**
 * The interface with a consumer of {@link ResourceSummary} information.
 * 
 */
public interface ResourceSummaryConsumer {

	/**
	 * Provide the consumer with the latest summary information.
	 * 
	 * @param summary
	 *            the most recent summary information. Not null.
	 */
	public void updateState(final ResourceSummary summary);
}
