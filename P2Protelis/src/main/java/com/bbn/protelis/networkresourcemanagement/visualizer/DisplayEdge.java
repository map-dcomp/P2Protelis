package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;

import com.bbn.protelis.networkresourcemanagement.Link;

public class DisplayEdge {
	private final DisplayNode head;

	public DisplayNode getHead() {
		return head;
	}

	private final DisplayNode tail;

	public DisplayNode getTail() {
		return tail;
	}

	private final Link link;

	public Link getLink() {
		return link;
	}

	public DisplayEdge(final Link link, final DisplayNode head, final DisplayNode tail) {
		this.link = link;
		this.head = head;
		this.tail = tail;
	}

//	protected static final Paint BLUE = new Color(0, 0, 255);
//	protected static final Paint RED = new Color(255, 0, 0);
//	protected static final Paint BLACK = new Color(0, 0, 0);
	private static final Paint GREY = new Color(200, 200, 200);

	public Paint getEdgeColor() {
		return GREY;
	}
}
