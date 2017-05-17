package com.bbn.protelis.processmanagement.testbed.visualizer;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import org.apache.commons.collections15.Transformer;
import org.protelis.lang.datatype.DeviceUID;

import com.bbn.protelis.common.visualizer.MultiVertexRenderer;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.Scenario;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class ScenarioVisualizer extends JApplet {
    // Serialization inherited from JApplet
    private static final long serialVersionUID = 1657620527127657878L;

    private static final int DEFAULT_WIDTH = 1200;//640;//1920;
    private static final int DEFAULT_HEIGHT = 800;//480;//1080;
    private static final int REFRESH_RATE = 100;//500
    
    ProcessStatus status = ProcessStatus.init;
    private Timer refresher;

    Graph<DisplayNode,DisplayEdge> g = new SparseMultigraph<DisplayNode,DisplayEdge>();

    // Graph contents
    private Map<DeviceUID,DisplayNode> nodes = new HashMap<>();
    Set<DisplayEdge> edges = new HashSet<DisplayEdge>();
    
    public ScenarioVisualizer(Scenario scenario) {
        // Add the nodes and edges
        createGraphFromNetwork(scenario.network);
        // Configure the rendering environment
        VisualizationViewer<DisplayNode,DisplayEdge> vv = configureGraphRendering();
        // set up Swing components
        initializeSwingComponents(scenario,vv);
        
        status = ProcessStatus.run;
    }

    private void createGraphFromNetwork(DaemonWrapper[] daemons) {
        // First add nodes to collection, so they'll be there for edge addition
        for(DaemonWrapper d : daemons) { 
            DisplayNode n = new DisplayNode(d);
            g.addVertex(n);
            nodes.put(n.getUID(),n); 
        }
        refreshEdges();
    }

    private void refreshEdges() {
        // TODO: only adjust changed edges, rather than all edges
        
        // First, discard all current edges
        for(DisplayEdge e : edges) {g.removeEdge(e); }
        // Next, add all missing edges
        for(DisplayNode n : nodes.values()) {
            for(Entry<DeviceUID, DisplayEdge.EdgeType> id : n.getNeighbors().entrySet()) {
                if(nodes.get(id.getKey()) != null) {
                    DisplayEdge e = new DisplayEdge(n, nodes.get(id.getKey()), id.getValue());
                    g.addEdge(e, e.head, e.tail, EdgeType.DIRECTED);
                    edges.add(e);
                }
            }
        }
    }

    private VisualizationViewer<DisplayNode,DisplayEdge> configureGraphRendering() {
        //Layout<DisplayNode,DisplayEdge> layout = new KKLayout<DisplayNode, DisplayEdge>(g);
        Layout<DisplayNode,DisplayEdge> layout = new ISOMLayout<DisplayNode, DisplayEdge>(g);
        layout.setSize(new Dimension((int)(DEFAULT_WIDTH*0.9),(int)(DEFAULT_HEIGHT*0.9))); // sets the initial size of the space
        VisualizationViewer<DisplayNode,DisplayEdge> vv = new VisualizationViewer<DisplayNode,DisplayEdge>(layout);
        vv.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        
        vv.getRenderer().setVertexRenderer(new MultiVertexRenderer<DisplayNode,DisplayEdge>());
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<DisplayNode,String>() {
            @Override
            public String transform(DisplayNode dn) {
                return dn.getVertexLabel();
            }
        });
        // Place labels at bottom center
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);
         
        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<DisplayNode,Paint>() {
            @Override
            public Paint transform(DisplayNode dn) {
                return null;
            }
        });
        
        vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<DisplayNode,Paint>() {
            @Override
            public Paint transform(DisplayNode dn) {
                return dn.getVertexColor();
                
            }
        });
        
        vv.getRenderContext().setVertexStrokeTransformer(new Transformer<DisplayNode,Stroke>() {
            @Override
            public Stroke transform(DisplayNode dn) {
                return new BasicStroke(3);
            }
        });
        
        Transformer<DisplayEdge, Paint> arrowPaint = new Transformer<DisplayEdge, Paint>() {
            @Override
            public Paint transform(DisplayEdge e) {
                return e.getEdgeColor();
            }
        };
        vv.getRenderContext().setEdgeDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowDrawPaintTransformer(arrowPaint);
        vv.getRenderContext().setArrowFillPaintTransformer(arrowPaint);

        vv.getRenderContext().setVertexShapeTransformer(new Transformer<DisplayNode,Shape>() {
            @Override
            public Shape transform(DisplayNode dn) {
                // The first 2 arguments here had better be half of the height and width respectively or it
                // screws up Jung's attempt to draw the arrows on directed edges.
                //return new Rectangle(-19,-25,38,50); // size of icon
                return new Rectangle(-22,-28,44,56); // slightly bigger than icon
            }
        });
        
        vv.getRenderContext().setVertexIconTransformer(new Transformer<DisplayNode,Icon>() {
            @Override
            public Icon transform(DisplayNode dn) {
                return dn.getIcon();
            }
        });

        vv.addGraphMouseListener(new GraphMouseListener<DisplayNode>() {
            @Override
            public void graphClicked(DisplayNode v, MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON1) {
                    v.handleClick();
                    vv.repaint();
                }
                me.consume();
            }

            @Override
            public void graphPressed(DisplayNode v, MouseEvent me) {
            }

            @Override
            public void graphReleased(DisplayNode v, MouseEvent me) {
            }
        });
        
        return vv;
    }

    private void initializeSwingComponents(Scenario scenario, VisualizationViewer<DisplayNode,DisplayEdge> vv) {
        JFrame frame = new JFrame("Graph View: "+scenario.scenario_name);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // The the display that it should kill the remote nodes on window close.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                status = ProcessStatus.stop;
            }
        });
        
        JPanel jungPanel = new JPanel();
        jungPanel.setLayout(new BoxLayout(jungPanel, BoxLayout.Y_AXIS));
        
        JPanel envPanel = new JPanel();
        envPanel.setLayout(new BoxLayout(envPanel, BoxLayout.X_AXIS));
        // Add a button to the frame to allow the user to toggle each specified global variable.
        for(String var : scenario.environmentButtons) {
            JToggleButton toggle = new JToggleButton(var);
            
            toggle.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    AbstractButton abstractButton = (AbstractButton)event.getSource();
                    boolean selected = abstractButton.getModel().isSelected();
                    for(DisplayNode dn : nodes.values()) {
                        dn.setEnvironmentVariable(var,selected);
                    }
                }
            });

            envPanel.add(toggle);
            toggle.setSelected(false);
        }
        
        jungPanel.add(vv);
        
        JPanel masterPanel = new JPanel();
        
        masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
        masterPanel.add(jungPanel);
        masterPanel.add(envPanel);
        
        frame.add(masterPanel);
        frame.pack();
        frame.setVisible(true);
        
        // TODO: figure out if this needs to be here
//      for(DisplayNode dn : g.getVertices()) {
//          vv.getRenderContext().getVertexIconTransformer().transform(dn);
//      }
        
        refresher = new Timer(REFRESH_RATE,
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshEdges();
                vv.repaint();
            }   
        });
        refresher.start();
    }

    public ProcessStatus getProcessStatus() {
        return status;
    }

    @Override
    public void stop() {
        refresher.stop();
        super.stop();
    }
}
