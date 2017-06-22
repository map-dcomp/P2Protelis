package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

/**
 * Identifier for a service. Implementations should be immutable and must be
 * {@link Comparable}.
 */
public interface ServiceIdentifier<T> extends Serializable {
    
    T getIdentifier();

}
