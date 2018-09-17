package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

/**
 * Attributes for metrics on a node. Implementations should be immutable and
 * must be {@link Comparable}.
 * 
 * @param <T>
 *            see {@link #getIdentifier()}
 */
public interface NodeAttribute<T> extends Serializable {

    /**
     * @return the object being wrapped or itself
     */
    T getAttribute();

}
