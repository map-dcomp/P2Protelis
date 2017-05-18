package com.bbn.protelis.utils;

import org.protelis.lang.datatype.DeviceUID;

/** Simple integer UIDs. */
public class IntegerUID implements DeviceUID {
    private static final long serialVersionUID = 1L;
    private final int uid;

    public IntegerUID(final int uid) {
        this.uid = uid;
    }

    public int getUID() {
        return uid;
    }

    public boolean equals(final IntegerUID alt) {
        return this.uid == alt.uid;
    }

    @Override
    public String toString() {
        return Integer.toString(uid);
    }
}
