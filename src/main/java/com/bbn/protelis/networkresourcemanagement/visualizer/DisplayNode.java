package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.NetworkNode;
import com.bbn.protelis.networkresourcemanagement.NetworkServer;

/**
 * An object for displaying a {@link NetworkNode}.
 */
public class DisplayNode {

    // Image collection:
    private static final Icon SERVER_SINGLE = new ImageIcon(DisplayNode.class.getResource("/server-small-green.png"));
    private static final Icon SERVER_POOL = new ImageIcon(DisplayNode.class.getResource("/server-small-yellow.png"));
    private static final Icon CLIENT = new ImageIcon(DisplayNode.class.getResource("/server-small-blue.png"));

    private final NetworkNode node;

    /**
     * @return the node
     */
    @Nonnull
    protected final NetworkNode getNode() {
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

        return builder.toString();
    }

    /**
     * @return the color to draw the object
     */
    public Paint getVertexColor() {
        final Color c;
        if (node instanceof NetworkServer) {
            final NetworkServer server = (NetworkServer) node;

            final float r = objectToColorComponent(server.getExecutionEnvironment().get("red"));
            final float g = objectToColorComponent(server.getExecutionEnvironment().get("green"));
            final float b = objectToColorComponent(server.getExecutionEnvironment().get("blue"));
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
    public Icon getIcon() {
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
