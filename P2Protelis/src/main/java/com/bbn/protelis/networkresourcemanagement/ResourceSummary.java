package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

/**
 * Summary state over some number of devices for some time.
 * 
 */
public interface ResourceSummary {

    /**
     * @return resource name -> resource value. Not null.
     * @see ResourceReport#getState()
     */
    public Map<String, Object> getState();

    /**
     * Merge an existing summary with this one. Creates a new object.
     * 
     * @param other
     *            the summary to merge. Not null.
     * @return a newly created summary. Not null.
     */
    public ResourceSummary merge(final ResourceSummary other);

    /**
     * Merge a {@link ResourceReport} with this one. Creates a new object.
     * 
     * @param report
     *            the report to merge. Not null.
     * @return a newly created summary. Not null.
     */
    public ResourceSummary merge(final ResourceReport report);

}
