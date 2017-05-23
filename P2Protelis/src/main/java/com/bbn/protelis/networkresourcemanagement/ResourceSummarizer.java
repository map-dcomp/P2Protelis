package com.bbn.protelis.networkresourcemanagement;

/**
 * Summarize resource information.
 *
 */
public interface ResourceSummarizer {

        /**
         * Merge an two summaries.
         * 
         * @return a newly created summary. Not null.
         */
        public ResourceSummary merge(final ResourceSummary one, final ResourceSummary two);

        /**
         * Merge a {@link ResourceReport} with a summary.
         * 
         * @param summary the summary to merge with the report. Not null.
         * @param report
         *            the report to merge. Not null.
         * @return a newly created summary. Not null.
         */
        public ResourceSummary merge(final ResourceSummary summary, final ResourceReport report);

        /**
         * Produce the empty/null {@link ResourceSummary}.
         */
        public ResourceSummary nullSummary();
        
}
