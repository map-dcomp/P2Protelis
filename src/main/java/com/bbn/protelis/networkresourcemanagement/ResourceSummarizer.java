package com.bbn.protelis.networkresourcemanagement;

/**
 * Summarize resource information.
 *
 */
public interface ResourceSummarizer {

    /**
     * Merge an two summaries.
     * 
     * @param one
     *            the first summary to merge. Not null.
     * @param two
     *            the second summary to merge. Not null.
     * @return a newly created summary. Not null, but may be the result of
     *         {@link #nullSummary()}.
     */
    ResourceSummary merge(ResourceSummary one, ResourceSummary two);

    /**
     * Merge a {@link ResourceReport} with a summary.
     * 
     * @param summary
     *            the summary to merge with the report. Not null.
     * @param report
     *            the report to merge. Not null.
     * @return a newly created summary. Not null.
     */
    ResourceSummary merge(ResourceSummary summary, ResourceReport report);

    /**
     * @return the empty/null {@link ResourceSummary}.
     */
    ResourceSummary nullSummary();

}
