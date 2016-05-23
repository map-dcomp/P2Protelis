package com.bbn.protelis.processmanagement.testbed.visualizer;

import java.awt.Color;
import java.awt.Paint;

public class DisplayEdge {
	public static enum EdgeType {PHYSICAL, LOGICAL, BOTH };
	
	DisplayNode head;
	DisplayNode tail;
	EdgeType type;
	
	public DisplayEdge(DisplayNode head, DisplayNode tail, EdgeType type) { 
		this.head = head; this.tail = tail; this.type = type;
	}

	protected static final Paint BLUE = new Color(0, 0, 255);
	protected static final Paint RED = new Color(255, 0, 0);
	protected static final Paint BLACK = new Color(0, 0, 0);
	protected static final Paint GREY = new Color(200, 200, 200);
	
	public Paint getEdgeColor() {
		switch(type) {
		case PHYSICAL:
			return GREY;
		case LOGICAL:
			return RED;
		case BOTH:
			return BLUE;
		default: // Should never appear
			return BLACK;
		}
	}
}
