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

import javax.annotation.Nonnull;
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
import com.bbn.protelis.networkresourcemanagement.NetworkClient;
import com.bbn.protelis.networkresourcemanagement.NetworkLink;
import com.bbn.protelis.networkresourcemanagement.NetworkNode;
import com.bbn.protelis.networkresourcemanagement.NetworkServer;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Visualizer for a {@link Scenario}. Call {@link #start()} to open the
 * visualization.
 *
 * @param <N>
 *            the server type
 * @param <L>
 *            the link type
 * @param <DN>
 *            the server display type
 * @param <DL>
 *            the link display type
 * @param <C>
 *            the client type
 */
public class ScenarioVisualizer<DN extends DisplayNode, DL extends DisplayEdge, L extends NetworkLink, N extends NetworkServer, C extends NetworkClient>
        extends JApplet {
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
    private final NetworkVisualizerFactory<DN, DL> visFactory;

    private JFrame frame;
    private VisualizationViewer<DN, DL> vv;
    private Timer refresher;

    private final Graph<DN, DL> g = new SparseMultigraph<>();

    private boolean closed = false;

    /**
     * 
     * @return true if the window has been closed
     */
    public boolean isClosed() {
        return closed;
    }

    // Graph contents
    private Map<DeviceUID, DN> nodes = new HashMap<>();
    private Set<DL> edges = new HashSet<>();
    private final Scenario<N, L, C> scenario;

    /**
     * @return the scenario that is being visualized
     */
    @Nonnull
    public final Scenario<N, L, C> getScenario() {
        return scenario;
    }

    /**
     * Create a visualization.
     * 
     * @param scenario
     *            the scenario to visualize
     * @param visFactory
     *            the factory for creating display objects
     */
    public ScenarioVisualizer(final Scenario<N, L, C> scenario, final NetworkVisualizerFactory<DN, DL> visFactory) {
        this.visFactory = visFactory;
        this.scenario = scenario;

        // Add the nodes and edges
        createGraphFromNetwork();
        // Configure the rendering environment
        configureGraphRendering();
        // set up Swing components
        initializeSwingComponents();
    }

    private void createGraphFromNetwork() {
        // First add nodes to collection, so they'll be there for edge addition
        for (final Map.Entry<DeviceUID, N> entry : scenario.getServers().entrySet()) {
            addNode(entry.getValue());
        }
        for (final Map.Entry<DeviceUID, C> entry : scenario.getClients().entrySet()) {
            addNode(entry.getValue());
        }
        refreshEdges();
    }

    private DN addNode(final NetworkNode node) {
        final DN n = visFactory.createDisplayNode(node);
        g.addVertex(n);
        nodes.put(n.getUID(), n);
        return n;
    }

    private void refreshEdges() {
        // First, discard all current edges
        for (final DL e : edges) {
            g.removeEdge(e);
        }
        edges.clear();

        // Next, add all edges
        for (final L l : scenario.getLinks()) {
            final DN leftNode = nodes.get(l.getLeft().getNodeIdentifier());
            if (null == leftNode) {
                throw new RuntimeException("Link " + l.getName() + " refers to node "
                        + l.getLeft().getNodeIdentifier().getName() + " that isn't in the graph.");
            }
            final DN rightNode = nodes.get(l.getRight().getNodeIdentifier());
            if (null == rightNode) {
                throw new RuntimeException("Link " + l.getName() + " refers to node "
                        + l.getRight().getNodeIdentifier().getName() + " that isn't in the graph.");
            }

            final DL edge = visFactory.createDisplayLink(l, leftNode, rightNode);
            g.addEdge(edge, leftNode, rightNode);
            edges.add(edge);
        }

    }

    private void configureGraphRendering() {
        // Layout<DisplayNode,DisplayEdge> layout = new KKLayout<DisplayNode,
        // DisplayEdge>(g);
        final Layout<DN, DL> layout = new ISOMLayout<DN, DL>(g);
        layout.setSize(new Dimension(LAYOUT_WIDTH, LAYOUT_HEIGHT)); // sets
                                                                    // the
                                                                    // initial
                                                                    // size
                                                                    // of
                                                                    // the
                                                                    // space
        vv = new VisualizationViewer<DN, DL>(layout);
        vv.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        vv.getRenderer().setVertexRenderer(new MultiVertexRenderer<DN, DL>());
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<DN, String>() {
            @Override
            public String transform(final DN dn) {
                return dn.getVertexLabel();
            }
        });
        // Place labels at bottom center
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);

        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<DN, Paint>() {
            @Override
            public Paint transform(final DN dn) {
                return null;
            }
        });

        vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<DN, Paint>() {
            @Override
            public Paint transform(final DN dn) {
                return dn.getVertexColor();

            }
        });

        vv.getRenderContext().setVertexStrokeTransformer(new Transformer<DN, Stroke>() {
            @Override
            public Stroke transform(final DN dn) {
                return new BasicStroke(3);
            }
        });

        final Transformer<DL, Paint> arrowPaint = new Transformer<DL, Paint>() {
            @Override
            public Paint transform(final DL e) {
                return e.getEdgeColor();
            }
        };
        vv.getRenderContext().setEdgeDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowFillPaintTransformer(arrowPaint);

        vv.getRenderContext().setVertexShapeTransformer(new Transformer<DN, Shape>() {
            @Override
            public Shape transform(final DN dn) {
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

        vv.getRenderContext().setVertexIconTransformer(new Transformer<DN, Icon>() {
            @Override
            public Icon transform(final DN dn) {
                return dn.getIcon();
            }
        });

        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<DL, String>() {
            @Override
            public String transform(final DL e) {
                return e.getLink().getName();
            }
        });

        // Create a graph mouse and add it to the visualization component
        final DefaultModalGraphMouse<DN, DL> gm = new DefaultModalGraphMouse<>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);

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
    }

    /**
     * Wait for the visualizer to close.
     */
    @SuppressFBWarnings(value = { "UW_UNCOND_WAIT",
            "WA_NOT_IN_LOOP" }, justification = "There isn't a conditional that we can check here")
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

    private void initializeSwingComponents() {
        frame = new JFrame("Graph View: " + scenario.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // The the display that it should kill the remote nodes on window close.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                // status = ProcessStatus.stop;
                closed = true;
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
    }

    @Override
    public void start() {
        super.start();

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
