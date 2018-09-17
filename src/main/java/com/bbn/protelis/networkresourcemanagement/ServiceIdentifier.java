package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

/**
 * Identifier for a service. Implementations should be immutable and must be
 * {@link Comparable}.
 * 
 * @param <T>
 *            the type of the object being wrapped or possibly itself
 */
public interface ServiceIdentifier<T> extends Serializable {

    /**
     * @return the object being wrapped or itself
     */
    T getIdentifier();

}
