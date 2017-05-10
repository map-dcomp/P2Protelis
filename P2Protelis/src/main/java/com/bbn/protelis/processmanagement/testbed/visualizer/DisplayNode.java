package com.bbn.protelis.processmanagement.testbed.visualizer;

import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ExecutionEnvironment;

import java.awt.Color;
import java.awt.Paint;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.bbn.protelis.processmanagement.daemon.LongDeviceUID;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.bbn.protelis.processmanagement.testbed.daemon.AbstractDaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.daemon.AbstractDaemonWrapper.Listener;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.visualizer.DisplayEdge.EdgeType;

public class DisplayNode implements Listener {
	// Image collection:
	static final Icon serverRun = new ImageIcon(DisplayNode.class.getResource("/server-small-green.png"));
	static final Icon serverHung = new ImageIcon(DisplayNode.class.getResource("/server-small-red.png"));
	static final Icon serverCompromised = new ImageIcon(DisplayNode.class.getResource("/server-small-orange.png"));
	static final Icon serverContaminated = new ImageIcon(DisplayNode.class.getResource("/server-small-yellow.png"));
	static final Icon serverStop = new ImageIcon(DisplayNode.class.getResource("/server-small-blue.png"));
	static final Icon serverInit = new ImageIcon(DisplayNode.class.getResource("/server-small-purple.png"));
	static final Icon serverShutdown = new ImageIcon(DisplayNode.class.getResource("/server-small-purple.png"));
	static final Icon serverNull = new ImageIcon(DisplayNode.class.getResource("/server-small.png"));
	
	AbstractDaemonWrapper source;
	private Map<DeviceUID,EdgeType> neighbors = new HashMap<>();
	private String vertexLabel;
	
	public DisplayNode(DaemonWrapper d) {
		ensureInitialized();
		
		source = (AbstractDaemonWrapper)d;
		source.addListener(this);
		setVertexLabel(source.alias);
		// TODO sort out labels
	}
	
	public Map<DeviceUID,EdgeType> getNeighbors() { return neighbors; }
	
	public DeviceUID getUID() {
		return new LongDeviceUID(source.getUID());
	}

	public String getVertexLabel() {
		return vertexLabel;
	}
	public void setVertexLabel(String label) {
		// TODO Auto-generated method stub
		vertexLabel = label;
	}

	public Paint getVertexColor() {
		float r = objectToColorComponent(source.getEnvironment().get("red"));
		float g = objectToColorComponent(source.getEnvironment().get("green"));
		float b = objectToColorComponent(source.getEnvironment().get("blue"));
		if(r==0 && g==0 && b==0) return null;
		return new Color(r,g,b);
	}
	private float objectToColorComponent(Object o) {
		if(o==null) { 
			return 0; 
		} else if(o instanceof Number) {
			return ((Number)o).floatValue();
		} else if(o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		} else {
			throw new IllegalArgumentException("Display doesn't know how make a color from "+o);
		}
	}

	public Icon getIcon() {
		ProcessStatus curStatus = source.getProcessStatus();
		switch(curStatus) {
		case hung: 			return serverHung;
		case init: 			return serverInit;
		case run: 			return serverRun;
		case compromised: 	return serverCompromised;
		case contaminated: 	return serverContaminated;
		case shutdown: 		return serverShutdown;
		case stop:			return serverStop;
		default:			return serverNull; // shouldn't happen
		}
	}

	public void handleClick() {
		// TODO: previously, each action was followed by:
		// vv.getRenderContext().getVertexIconTransformer().transform(this);
		// Figure out if this was necessary

		switch(source.getProcessStatus()) {
		case stop:
			source.signalProcess(ProcessStatus.init);
			break;
		case init:
		case run:
		case shutdown:
			source.signalProcess(ProcessStatus.hung);
			break;
		case hung:
			source.signalProcess(ProcessStatus.shutdown);
			break;
		}
	}

	/**
	 * Called by the visualizer to set the environment variable "var" to boolean value "selected" on all daemon devices
	 * @param var
	 * @param selected
	 */
	public void setEnvironmentVariable(String var, boolean selected) {
		source.getEnvironment().put(var, selected);
	}

	@Override
	public void daemonUpdated(AbstractDaemonWrapper d) {
		updateStatus();
		Set<DeviceUID> logicalNeighbors = d.getLogicalNeighbors();
		Set<DeviceUID> physicalNeighbors = d.getPhysicalNeighbors();

		Map<DeviceUID,EdgeType> newNbrs = new HashMap<>();
		if(logicalNeighbors!=null)
			logicalNeighbors.forEach((id) -> { newNbrs.put(id,EdgeType.LOGICAL); });
		if(physicalNeighbors!=null)
			physicalNeighbors.forEach((id) -> { 
				if(newNbrs.containsKey(id)) { newNbrs.put(id,EdgeType.BOTH);} 
				else {newNbrs.put(id,EdgeType.PHYSICAL);} 
			});
		neighbors = newNbrs;
	}

	// This gets called on the DisplayNode whenever its Remote has a report.
	void updateStatus() {
		String debugStr = debugString();
		Object value = source.getValue();
		String valueStr= (value==null) ? "<null>" : value.toString();
		setVertexLabel("<html>"+" #"+source.getUID()+": <b>" + source.alias + "</b><br><hr>" + valueStr + debugStr);
		// TODO: figure out dependencies
//		try {
//			if(r.getReport().debug.get("deps") != null) {
//				updateDependencyEdges((Tuple)r.getReport().debug.get("deps"));
//			}
//		} catch(ClassCastException e) {
//			//e.printStackTrace();
//		}
	}
	
	private static final Set<FasterString> ignores = new HashSet<>();
	private String debugString() {
		boolean hasAny = false;
		String s = "<br><font color=\"444444\"><small><i>";
		
		// TODO: need to have key set exposed for iteration; for now, disabling
//		ExecutionEnvironment env = source.getEnvironment();
//		for(Iterator<FasterString> i=env.keySet().iterator(); i.hasNext();) {
//			FasterString k = i.next();
//			if(!ignores.contains(k)) { hasAny = true; s += "<br>"+k+" = "+env.get(k); }
//		}
		// No entries = no display
		if(!hasAny) return ""; 
		s += "</i></small></font>";
		return s;
	}
	
	static private boolean initialized = false;
	private void ensureInitialized() {
		if(initialized) return;
		// Set up ignores
		Arrays.asList("red", "green", "blue", "logicalNeighbors")
			.forEach((s) -> ignores.add(new FasterString(s)));
		initialized = true;
	}
	public static void ignore(String s) { ignores.add(new FasterString(s)); }
}
