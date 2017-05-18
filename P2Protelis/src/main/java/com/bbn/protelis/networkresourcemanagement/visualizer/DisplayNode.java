package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * An object for displaying a {@link Node}.
 */
public class DisplayNode {
    // Image collection:
    static final Icon SERVER_RUN = new ImageIcon(DisplayNode.class.getResource("/server-small-green.png"));

    private final Node node;
    private Set<DeviceUID> neighbors = new HashSet<>();
    private String vertexLabel;

    /**
     * 
     * @param n
     *            the node to be displayed
     */
    public DisplayNode(final Node n) {
        node = n;
        setVertexLabel(node.getName());

        for (final DeviceUID neighbor : node.getNeighbors()) {
            neighbors.add(neighbor);
        }
    }

    /**
     * 
     * @return the uid of the node
     */
    public DeviceUID getUID() {
        return node.getDeviceUID();
    }

    /**
     * @return the label for the object
     */
    public String getVertexLabel() {
        return vertexLabel;
    }

    /**
     * 
     * @param label
     *            the label for the object
     */
    public void setVertexLabel(final String label) {
        vertexLabel = label;
    }

    /**
     * 
     * @return the color to draw the object
     */
    public Paint getVertexColor() {
        return Color.BLACK;
    }

    /**
     * 
     * @return the icon to use for the object
     */
    public Icon getIcon() {
        return SERVER_RUN;
    }

}
