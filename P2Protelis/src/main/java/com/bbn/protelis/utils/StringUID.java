package com.bbn.protelis.utils;

import org.protelis.lang.datatype.DeviceUID;

/** Simple string UIDs */
public class StringUID implements DeviceUID {
    private static final long serialVersionUID = 1L;
    private final String uid;

    public StringUID(final String uid) {
        this.uid = uid;
    }

    public String getUID() {
        return uid;
    }

    public boolean equals(final StringUID alt) {
        return this.uid == alt.uid;
    }

    @Override
    public String toString() {
        return uid;
    }
}
