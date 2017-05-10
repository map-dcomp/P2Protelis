package com.bbn.protelis.processmanagement.testbed.client;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = -7307941668294668610L;
	
	boolean incoming; // true for incoming, false for outgoing
	Object contents;
	
	/** Default constructor, for reading in serialized versions */
	Message() { }
	Message(boolean incoming, Object contents) { this.incoming = incoming; this.contents = contents; }
	
	public boolean isIncoming() {
		return incoming;
	}

	public Object getContents() {
		return contents;
	}
	
	public String toString() { 
		return (incoming?"i":"o")+":"+contents;
	};
	
	public int hashCode() { return contents.hashCode() + (incoming?1:0); }
	public boolean equals(Object o) {
		if(o instanceof Message) {
			return incoming == ((Message) o).incoming && contents.equals(((Message) o).contents);
		}
		return false;
	}
}
