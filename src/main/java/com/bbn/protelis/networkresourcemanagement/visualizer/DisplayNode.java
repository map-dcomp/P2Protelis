/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.NetworkNode;
import com.bbn.protelis.networkresourcemanagement.NetworkServer;

/**
 * An object for displaying a {@link NetworkNode}.
 */
public class DisplayNode {

    /**
     * Icon for single servers.
     */
    public static final ImageIcon SERVER_SINGLE = new ImageIcon(
            DisplayNode.class.getResource("/server-small-green.png"));
    /**
     * Icon for pooled servers.
     */
    public static final ImageIcon SERVER_POOL = new ImageIcon(
            DisplayNode.class.getResource("/server-small-yellow.png"));
    /**
     * Icon for clients.
     */
    public static final ImageIcon CLIENT = new ImageIcon(DisplayNode.class.getResource("/server-small-blue.png"));

    private final NetworkNode node;

    /**
     * @return the node
     */
    @Nonnull
    public final NetworkNode getNode() {
        return node;
    }

    /**
     * 
     * @param n
     *            the node to be displayed
     */
    public DisplayNode(@Nonnull final NetworkNode n) {
        node = n;
    }

    /**
     * 
     * @return the uid of the node
     */
    public DeviceUID getUID() {
        return node.getNodeIdentifier();
    }

    /**
     * @return the label for the object
     */
    public String getVertexLabel() {
        // final String debugStr = debugString();
        final StringBuilder builder = new StringBuilder();
        builder.append("<html>");

        builder.append("<b>" + node.getNodeIdentifier().getName() + "</b>");
        if (node instanceof NetworkServer) {
            final NetworkServer server = (NetworkServer) node;
            final String valueStr = Objects.toString(server.getVM().getCurrentValue());
            builder.append("<br><hr>" + valueStr);
        }

        builder.append("</html>");

        return builder.toString();
    }

    /**
     * @return the color to draw the object
     */
    public Paint getVertexColor() {
        final Color c;
        if (node instanceof NetworkServer) {
            final NetworkServer server = (NetworkServer) node;

            final float r = objectToColorComponent(server.getEnvironment().get("red"));
            final float g = objectToColorComponent(server.getEnvironment().get("green"));
            final float b = objectToColorComponent(server.getEnvironment().get("blue"));
            if (r == 0 && g == 0 && b == 0) {
                return null;
            }
            c = new Color(r, g, b);
        } else {
            c = Color.BLACK;
        }

        return c;
    }

    private float objectToColorComponent(final Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Long || o instanceof Integer) {
            final int value = ((Number) o).intValue();
            final float limitedValue = Math.max(0, Math.min(255, value));
            final float scaledValue = limitedValue / Color.WHITE.getRed();
            return scaledValue;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? 1 : 0;
        } else {
            throw new IllegalArgumentException("DisplayNode doesn't know how make a color from " + o);
        }
    }

    /**
     * 
     * @return the icon to use for the object
     */
    public ImageIcon getIcon() {
        if (node instanceof NetworkServer) {
            final NetworkServer server = (NetworkServer) node;
            if (server.isPool()) {
                return SERVER_POOL;
            } else {
                return SERVER_SINGLE;
            }
        } else {
            return CLIENT;
        }
    }

}
