package com.bbn.protelis.processmanagement.daemon;

import org.protelis.lang.datatype.DeviceUID;

public class LongDeviceUID implements DeviceUID {
    private static final long serialVersionUID = 6002429193039916210L;
    
    public long id;
    public LongDeviceUID(long id) { this.id = id; }
    
    public boolean equals(Object o) {
        if(o instanceof LongDeviceUID) {
            return ((LongDeviceUID)o).id == id;
        }
        return false;
    }
    public int hashCode() {
        return (int) id;
    }
    public String toString() { return "{ID:"+id+"}"; }
}
