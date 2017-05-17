package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.Color;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.networkresourcemanagement.Node;

public class DisplayNode {
    // Image collection:
    static final Icon serverRun = new ImageIcon(DisplayNode.class.getResource("/server-small-green.png"));
    static final Icon serverHung = new ImageIcon(DisplayNode.class.getResource("/server-small-red.png"));
    static final Icon serverCompromised = new ImageIcon(DisplayNode.class.getResource("/server-small-orange.png"));
    static final Icon serverContaminated = new ImageIcon(DisplayNode.class.getResource("/server-small-yellow.png"));
    static final Icon serverStop = new ImageIcon(DisplayNode.class.getResource("/server-small-blue.png"));
    static final Icon serverInit = new ImageIcon(DisplayNode.class.getResource("/server-small-purple.png"));
    static final Icon serverShutdown = new ImageIcon(DisplayNode.class.getResource("/server-small-purple.png"));
    static final Icon serverNull = new ImageIcon(DisplayNode.class.getResource("/server-small.png"));

    private final Node node;
    private Set<DeviceUID> neighbors = new HashSet<>();
    private String vertexLabel;

    public DisplayNode(final Node n) {
        ensureInitialized();

        node = n;
        setVertexLabel(node.getName());

        for (final DeviceUID neighbor : node.getNeighbors()) {
            neighbors.add(neighbor);
        }
    }

    public Set<DeviceUID> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    public DeviceUID getUID() {
        return node.getDeviceUID();
    }

    public String getVertexLabel() {
        return vertexLabel;
    }

    public void setVertexLabel(final String label) {
        vertexLabel = label;
    }

    public Paint getVertexColor() {
        return Color.BLACK;
    }

    public Icon getIcon() {
        return serverRun;
    }

    private static final Set<FasterString> ignores = new HashSet<>();

    static private boolean initialized = false;

    private void ensureInitialized() {
        if (initialized)
            return;
        // Set up ignores
        Arrays.asList("red", "green", "blue", "logicalNeighbors").forEach((s) -> ignores.add(new FasterString(s)));
        initialized = true;
    }

    public static void ignore(String s) {
        ignores.add(new FasterString(s));
    }
}
