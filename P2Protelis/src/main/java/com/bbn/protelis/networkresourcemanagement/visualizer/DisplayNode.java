package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * An object for displaying a {@link Node}.
 */
public class DisplayNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayNode.class);

    // Image collection:
    private static final Icon SERVER_RUN = new ImageIcon(DisplayNode.class.getResource("/server-small-green.png"));

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
     * Reads properties "red", "green" and "blue" from the execution
     * environment.
     * 
     * @return the color to draw the object
     */
    public Paint getVertexColor() {
        // final float r =
        // objectToColorComponent(node.getExecutionEnvironment().get("red"));
        // final float g =
        // objectToColorComponent(node.getExecutionEnvironment().get("green"));
        // final float b =
        // objectToColorComponent(node.getExecutionEnvironment().get("blue"));
        // if (r == 0 && g == 0 && b == 0) {
        // return null;
        // }
        // final Color c = new Color(r, g, b);

        final long executionCount = node.getExecutionCount();
        final Color c;
        if (executionCount % 2 == 0) {
            c = Color.RED;
        } else {
            c = Color.WHITE;
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Color: " + c + " count: " + executionCount);
        }
        
        return c;
    }
    //
    // private float objectToColorComponent(final Object o) {
    // if (o == null) {
    // return 0;
    // } else if (o instanceof Long || o instanceof Integer) {
    // final int value = ((Number) o).intValue();
    // final float limitedValue = Math.max(0, Math.min(255, value));
    // final float scaledValue = limitedValue / Color.WHITE.getRed();
    // return scaledValue;
    // } else if (o instanceof Number) {
    // return ((Number) o).floatValue();
    // } else if (o instanceof Boolean) {
    // return ((Boolean) o).booleanValue() ? 1 : 0;
    // } else {
    // throw new IllegalArgumentException("DisplayNode doesn't know how make a
    // color from " + o);
    // }
    // }

    /**
     * 
     * @return the icon to use for the object
     */
    public Icon getIcon() {
        return SERVER_RUN;
    }

}
