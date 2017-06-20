package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

/**
 * Identifier for a service. Implementations should be immutable. 
 */
public interface ServiceIdentifier extends Serializable, Comparable<ServiceIdentifier> {

}
