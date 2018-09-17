package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;

import com.bbn.protelis.networkresourcemanagement.NetworkLink;

/**
 * Display object for a {@link NetworkLink}.
 */
public class DisplayEdge {
    private final DisplayNode head;

    /**
     * @return one end of the edge
     */
    public DisplayNode getHead() {
        return head;
    }

    private final DisplayNode tail;

    /**
     * 
     * @return the other end of the edge
     */
    public DisplayNode getTail() {
        return tail;
    }

    private final NetworkLink link;

    /**
     * 
     * @return the underlying link object
     */
    public NetworkLink getLink() {
        return link;
    }

    /**
     * Construct a display object for a {@link NetworkLink}.
     * 
     * @param link the link
     * @param head the display object for the head
     * @param tail the display object for the tail
     */
    public DisplayEdge(final NetworkLink link, final DisplayNode head, final DisplayNode tail) {
        this.link = link;
        this.head = head;
        this.tail = tail;
    }

//  protected static final Paint BLUE = new Color(0, 0, 255);
//  protected static final Paint RED = new Color(255, 0, 0);
//  protected static final Paint BLACK = new Color(0, 0, 0);
    private static final Paint GREY = new Color(200, 200, 200);

    /**
     * 
     * @return the color to use when drawing the edge.
     */
    public Paint getEdgeColor() {
        return GREY;
    }
    
    /**
     * @return the text to display on the edge (link name)
     */
    public String getDisplayText() {
        return getLink().getName();
    }


}
