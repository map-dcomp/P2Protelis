package com.bbn.protelis.processmanagement.daemon;

import org.protelis.lang.datatype.DeviceUID;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public class LongDeviceUID implements DeviceUID {
    private static final long serialVersionUID = 6002429193039916210L;
    
    private long id;
    public LongDeviceUID(final long id) {
        this.id = id;
    }
    
    public boolean equals(final Object o) {
        if (o instanceof LongDeviceUID) {
            return ((LongDeviceUID)o).id == id;
        }
        return false;
    }
    public int hashCode() {
        return (int) id;
    }
    public String toString() { 
        return "{ID:" + id + "}";
    }
}
