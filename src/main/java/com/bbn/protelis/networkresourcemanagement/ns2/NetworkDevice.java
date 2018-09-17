package com.bbn.protelis.networkresourcemanagement.ns2;

import javax.annotation.Nonnull;

/**
 * A device in the topology.
 * 
 * @author jschewe
 *
 */
public abstract class NetworkDevice {

    /**
     * 
     * @param name
     *            see {@link #getName()}
     */
    public NetworkDevice(@Nonnull final String name) {
        this.name = name;
    }

    private final String name;

    /**
     * @return the name of the device
     */
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        } else if (this == o) {
            return true;
        } else if (getClass().equals(o.getClass())) {
            final NetworkDevice other = (NetworkDevice) o;
            return getName().equals(other.getName());
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "NetworkDevice " + getName();
    }

}
