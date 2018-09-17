package com.bbn.protelis.networkresourcemanagement;

/**
 * Opaque identifier for a container that runs tasks. Implementations should be
 * immutable and must be {@link Comparable}. Container names shall be compared
 * in a case-insensitive manner.
 */
public interface ContainerIdentifier extends NodeIdentifier {

}
