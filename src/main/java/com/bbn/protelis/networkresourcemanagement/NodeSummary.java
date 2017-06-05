package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Summary of information from a number of nodes for a service. This can be
 * either client demand, or server loaod.
 */
public class NodeSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<NodeAttribute, Double> summaryInformation = new HashMap<>();

    /**
     * 
     * @param info
     *            the information to store
     */
    public NodeSummary(final Map<NodeAttribute, Double> info) {
        this.summaryInformation.putAll(info);
    }

    /**
     * @return The summary information as an unmodifiable map
     */
    public Map<NodeAttribute, Double> getSummaryInformation() {
        return Collections.unmodifiableMap(this.summaryInformation);
    }
}
