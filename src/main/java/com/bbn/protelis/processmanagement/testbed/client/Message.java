package com.bbn.protelis.processmanagement.testbed.client;

import java.io.Serializable;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public class Message implements Serializable {
    private static final long serialVersionUID = -7307941668294668610L;
    
    private boolean incoming; // true for incoming, false for outgoing
    private Object contents;
    
    /** Default constructor, for reading in serialized versions */
    Message() { }
    Message(final boolean incoming, final Object contents) { 
        this.incoming = incoming; 
        this.contents = contents;
    }
    
    public boolean isIncoming() {
        return incoming;
    }

    public Object getContents() {
        return contents;
    }
    
    public String toString() { 
        return (incoming ? "i" : "o") + ":" + contents;
    };
    
    public int hashCode() { 
        return contents.hashCode() + (incoming ? 1 : 0);
    }
    public boolean equals(final Object o) {
        if (o instanceof Message) {
            return incoming == ((Message) o).incoming && contents.equals(((Message) o).contents);
        }
        return false;
    }
}
