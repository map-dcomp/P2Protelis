/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
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
