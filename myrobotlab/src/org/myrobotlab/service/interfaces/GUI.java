package org.myrobotlab.service.interfaces;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.myrobotlab.control.ServiceGUI;
import org.myrobotlab.framework.Service;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;


/**
 * Swing gui interface
 *
 */
public abstract class GUI extends Service  {

	private static final long serialVersionUID = 1L;
	
	public GUI(String instanceName, String serviceClass) {
		this(instanceName, serviceClass, null);
	}
	
	public GUI(String instanceName, String serviceClass, String inHost) {
		super(instanceName, serviceClass, inHost);
	}

	public abstract JTabbedPane loadTabPanels();	
	public abstract JFrame getFrame();
	public abstract void pack();
	//public abstract void display(); PUT IN Service class
	
	public abstract void undockPanel(String boundServiceName);

	// for GUIServiceIn/OutMethodDialogs
	public abstract void setPeriod0(String s);
	public abstract void setPeriod1(String s);
	public abstract void setArrow(String s);

	public abstract void setDstServiceName(final String d);
	public abstract void setDstMethodName(final String d);
	public abstract void setSrcServiceName(final String d);
	public abstract void setSrcMethodName(final String d);

	public abstract String getDstServiceName();
	public abstract String getDstMethodName();
	public abstract String getSrcMethodName();
	public abstract String getSrcServiceName();
	
	public abstract mxGraph getGraph();
	public abstract HashMap<String, mxCell> getCells();
	
	public abstract HashMap<String, ServiceGUI> getServiceGUIMap(); 
	
	public abstract String getGraphXML();
	public abstract void setGraphXML(final String xml);

}
