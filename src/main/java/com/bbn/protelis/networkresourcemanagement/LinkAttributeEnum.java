package com.bbn.protelis.networkresourcemanagement;

/**
 * Used to specify the type of information being reported for capacity or usage
 * for a {@link NetworkLink}.
 * 
 */
public enum LinkAttributeEnum implements LinkAttribute<LinkAttributeEnum> {

    /**
     * Capacity or usage in bytes per second.
     */
    DATARATE;

    @Override
    public LinkAttributeEnum getAttribute() {
        return this;
    }

}
