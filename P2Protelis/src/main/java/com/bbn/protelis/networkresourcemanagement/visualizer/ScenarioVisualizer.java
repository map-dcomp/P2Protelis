package com.bbn.protelis.networkresourcemanagement.visualizer;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.collections15.Transformer;
import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.visualizer.MultiVertexRenderer;
import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Visualizer for a {@link Scenario}.
 *
 */
public class ScenarioVisualizer extends JApplet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioVisualizer.class);

    // Serialization inherited from JApplet
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_WIDTH = 1200;// 640;//1920;
    private static final int DEFAULT_HEIGHT = 800;// 480;//1080;
    private static final int LAYOUT_WIDTH = (int) (0.9 * DEFAULT_WIDTH);
    private static final int LAYOUT_HEIGHT = (int) (0.9 * DEFAULT_HEIGHT);
    private static final int REFRESH_RATE = 100;// 500
    private final Object closeLock = new Object();
    private volatile boolean frameOpen = false;

    private Timer refresher;

    private final Graph<DisplayNode, DisplayEdge> g = new SparseMultigraph<DisplayNode, DisplayEdge>();

    // Graph contents
    private Map<DeviceUID, DisplayNode> nodes = new HashMap<>();
    private Set<DisplayEdge> edges = new HashSet<DisplayEdge>();
    private final Scenario scenario;

    /**
     * Create a visualization.
     * 
     * @param scenario
     *            the scenario to visualize
     */
    public ScenarioVisualizer(final Scenario scenario) {
        this.scenario = scenario;

        // Add the nodes and edges
        createGraphFromNetwork();
        // Configure the rendering environment
        final VisualizationViewer<DisplayNode, DisplayEdge> vv = configureGraphRendering();
        // set up Swing components
        initializeSwingComponents(vv);
    }

    private void createGraphFromNetwork() {
        // First add nodes to collection, so they'll be there for edge addition
        for (final Map.Entry<DeviceUID, Node> entry : scenario.getNodes().entrySet()) {
            addNode(entry.getValue());
        }
        refreshEdges();
    }

    private DisplayNode addNode(final Node node) {
        final DisplayNode n = new DisplayNode(node);
        g.addVertex(n);
        nodes.put(n.getUID(), n);
        return n;
    }

    private void refreshEdges() {
        // First, discard all current edges
        for (final DisplayEdge e : edges) {
            g.removeEdge(e);
        }
        edges.clear();

        // Next, add all edges
        for (final Link l : scenario.getLinks()) {
            DisplayNode leftNode = nodes.get(l.getLeft().getDeviceUID());
            if (null == leftNode) {
                LOGGER.warn("Link " + l.getName() + " refers to node " + l.getLeft().getName()
                        + " that isn't in the graph, adding.");
                leftNode = addNode(l.getLeft());
            }
            DisplayNode rightNode = nodes.get(l.getRight().getDeviceUID());
            if (null == rightNode) {
                LOGGER.warn("Link " + l.getName() + " refers to node " + l.getRight().getName()
                        + " that isn't in the graph, adding.");
                rightNode = addNode(l.getRight());
            }

            final DisplayEdge edge = new DisplayEdge(l, leftNode, rightNode);
            g.addEdge(edge, leftNode, rightNode);
            edges.add(edge);
        }

    }

    private VisualizationViewer<DisplayNode, DisplayEdge> configureGraphRendering() {
        // Layout<DisplayNode,DisplayEdge> layout = new KKLayout<DisplayNode,
        // DisplayEdge>(g);
        Layout<DisplayNode, DisplayEdge> layout = new ISOMLayout<DisplayNode, DisplayEdge>(g);
        layout.setSize(new Dimension(LAYOUT_WIDTH, LAYOUT_HEIGHT)); // sets
                                                                    // the
                                                                    // initial
                                                                    // size
                                                                    // of
                                                                    // the
                                                                    // space
        VisualizationViewer<DisplayNode, DisplayEdge> vv = new VisualizationViewer<DisplayNode, DisplayEdge>(layout);
        vv.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        vv.getRenderer().setVertexRenderer(new MultiVertexRenderer<DisplayNode, DisplayEdge>());
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<DisplayNode, String>() {
            @Override
            public String transform(final DisplayNode dn) {
                return dn.getVertexLabel();
            }
        });
        // Place labels at bottom center
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);

        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<DisplayNode, Paint>() {
            @Override
            public Paint transform(final DisplayNode dn) {
                return null;
            }
        });

        vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<DisplayNode, Paint>() {
            @Override
            public Paint transform(final DisplayNode dn) {
                return dn.getVertexColor();

            }
        });

        vv.getRenderContext().setVertexStrokeTransformer(new Transformer<DisplayNode, Stroke>() {
            @Override
            public Stroke transform(final DisplayNode dn) {
                return new BasicStroke(3);
            }
        });

        Transformer<DisplayEdge, Paint> arrowPaint = new Transformer<DisplayEdge, Paint>() {
            @Override
            public Paint transform(final DisplayEdge e) {
                return e.getEdgeColor();
            }
        };
        vv.getRenderContext().setEdgeDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowFillPaintTransformer(arrowPaint);

        vv.getRenderContext().setVertexShapeTransformer(new Transformer<DisplayNode, Shape>() {
            @Override
            public Shape transform(final DisplayNode dn) {
                // The first 2 arguments here had better be half of the height
                // and width respectively or it
                // screws up Jung's attempt to draw the arrows on directed
                // edges.
                // return new Rectangle(-19,-25,38,50); // size of icon
                final Icon icon = dn.getIcon();

                // icon is 37x50
                final int rectangleWidth = icon.getIconWidth() + 7;
                final int rectangleHeight = icon.getIconHeight() + 6;
                final int rectangleX = -1 * rectangleWidth / 2;
                final int rectangleY = -1 * rectangleHeight / 2;
                return new Rectangle(rectangleX, rectangleY, rectangleWidth, rectangleHeight); // slightly
                                                                             // bigger
                                                                             // than
                // icon
            }
        });

        vv.getRenderContext().setVertexIconTransformer(new Transformer<DisplayNode, Icon>() {
            @Override
            public Icon transform(final DisplayNode dn) {
                return dn.getIcon();
            }
        });

        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<DisplayEdge, String>() {
            @Override
            public String transform(final DisplayEdge e) {
                return e.getLink().getName();
            }
        });

        // vv.addGraphMouseListener(new GraphMouseListener<DisplayNode>() {
        // @Override
        // public void graphClicked(DisplayNode v, MouseEvent me) {
        // if (me.getButton() == MouseEvent.BUTTON1) {
        // v.handleClick();
        // vv.repaint();
        // }
        // me.consume();
        // }
        //
        // @Override
        // public void graphPressed(DisplayNode v, MouseEvent me) {
        // }
        //
        // @Override
        // public void graphReleased(DisplayNode v, MouseEvent me) {
        // }
        // });

        return vv;
    }

    /**
     * Wait for the visualizer to close.
     */
    public void waitForClose() {
        if (frameOpen) {
            synchronized (closeLock) {
                try {
                    closeLock.wait();
                } catch (final InterruptedException e) {
                    LOGGER.debug("Got interrupted, should be time to shutdown", e);
                }
            }
        }
    }

    private void initializeSwingComponents(final VisualizationViewer<DisplayNode, DisplayEdge> vv) {
        final JFrame frame = new JFrame("Graph View: " + scenario.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // The the display that it should kill the remote nodes on window close.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                // status = ProcessStatus.stop;
                synchronized (closeLock) {
                    closeLock.notifyAll();
                }
            }
        });

        final JPanel jungPanel = new JPanel();
        jungPanel.setLayout(new BoxLayout(jungPanel, BoxLayout.Y_AXIS));

        // JPanel envPanel = new JPanel();
        // envPanel.setLayout(new BoxLayout(envPanel, BoxLayout.X_AXIS));
        // // Add a button to the frame to allow the user to toggle each
        // specified
        // // global variable.
        // for (String var : scenario.environmentButtons) {
        // JToggleButton toggle = new JToggleButton(var);
        //
        // toggle.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent event) {
        // AbstractButton abstractButton = (AbstractButton) event.getSource();
        // boolean selected = abstractButton.getModel().isSelected();
        // for (DisplayNode dn : nodes.values()) {
        // dn.setEnvironmentVariable(var, selected);
        // }
        // }
        // });
        //
        // envPanel.add(toggle);
        // toggle.setSelected(false);
        // }

        jungPanel.add(vv);

        final JPanel masterPanel = new JPanel();

        masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
        masterPanel.add(jungPanel);
        // masterPanel.add(envPanel);

        frame.add(masterPanel);
        frame.pack();
        frame.setVisible(true);
        frameOpen = true;

        // TODO: figure out if this needs to be here
        // for(DisplayNode dn : g.getVertices()) {
        // vv.getRenderContext().getVertexIconTransformer().transform(dn);
        // }

        refresher = new Timer(REFRESH_RATE, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                refreshEdges();
                vv.repaint();
            }
        });
        refresher.start();
    }

    @Override
    public void stop() {
        refresher.stop();
        super.stop();
    }
}
