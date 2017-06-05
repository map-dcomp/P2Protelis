package com.bbn.protelis.networkresourcemanagement;

/**
 * Used to specify the type of information being reported for capacity or usage
 * of a {@link Node}.
 * 
 */
public enum NodeAttribute {

    /**
     * CPU information. Measured in number of cores.
     */
    CPU,
    /**
     * Amount of memory in bytes.
     */
    MEMORY,
    /**
     * Amount of disk in bytes.
     */
    DISK,
    /**
     * Measured in units of standard small containers.
     */
    TASK_CONTAINERS

}
