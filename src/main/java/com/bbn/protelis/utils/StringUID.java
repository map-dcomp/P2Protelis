package com.bbn.protelis.utils;

import org.protelis.lang.datatype.DeviceUID;

/** Simple string UIDs. Will be replaced by next Protelis release */
@Deprecated
public class StringUID implements DeviceUID, Comparable<StringUID> {
    private static final long serialVersionUID = 1L;
    private final String uid;

    /**
     * @param uid
     *            the string to use as the UID
     */
    public StringUID(final String uid) {
        this.uid = uid;
    }

    /**
     * 
     * @return the underlying string
     */
    public String getUID() {
        return uid;
    }

    @Override
    public boolean equals(final Object alt) {
        if (this == alt) {
            return true;
        } else if (alt instanceof StringUID) {
            final StringUID other = (StringUID) alt;
            return this.uid.equals(other.uid);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.uid.hashCode();
    }

    @Override
    public String toString() {
        return uid;
    }

    @Override
    public int compareTo(final StringUID other) {
        return uid.compareTo(other.uid);
    }
}
