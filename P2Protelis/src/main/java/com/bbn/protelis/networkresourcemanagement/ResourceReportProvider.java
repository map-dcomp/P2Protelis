package com.bbn.protelis.networkresourcemanagement;

/**
 * The interface used by a consumer of {@link ResourceReport} information.
 */
public interface ResourceReportProvider {

    /**
     * Provide the consumer with the latest report information. Only call this
     * method once per cycle to make decisions on. Each time the method is
     * called a different result may be returned.
     * 
     * @return the most recent summary information. Not null.
     */
    ResourceSummary getResourceReport();
}
