/**
 *                    
 * @author greg (at) myrobotlab.org
 *  
 * This file is part of MyRobotLab (http://myrobotlab.org).
 *
 * MyRobotLab is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * MyRobotLab is distributed in the hope that it will be useful or fun,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * All libraries in thirdParty bundle are subject to their own license
 * requirements - please refer to http://myrobotlab.org/libraries for 
 * details.
 * 
 * Enjoy !
 * 
 * */

package org.myrobotlab.control;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.myrobotlab.framework.MRLListener;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.GUIService;
import org.slf4j.Logger;

public abstract class ServiceGUI {

	public final static Logger log = LoggerFactory.getLogger(ServiceGUI.class);
	public final String boundServiceName;
	public final GUIService myService;

	public GridBagConstraints gc = new GridBagConstraints();
	public JPanel display = new JPanel();
	
	public TabControl2 tabControl;

	public abstract void init();

	public ServiceGUI(final String boundServiceName, final GUIService myService) {
		this.boundServiceName = boundServiceName;
		this.myService = myService;
		this.tabControl = new TabControl2(myService, myService.tabs, display, boundServiceName);
		
		gc.anchor = GridBagConstraints.FIRST_LINE_END;

		// place menu
		gc.gridx = 0;
		gc.gridy = 0;
		display.setLayout(new GridBagLayout());
		// gc.fill = GridBagConstraints.HORIZONTAL;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;

	}

	/**
	 * hook for GUIService framework to query each panel before release checking if any
	 * panel needs user input before shutdown
	 * 
	 * @return
	 */
	public boolean isReadyForRelease() {
		return true;
	}

	
	public JPanel getDisplay() {
		return display;
	}

	/*
	 * Service functions
	 */
	public void subscribe(String inOutMethod) {
		subscribe(inOutMethod, inOutMethod, (Class<?>[]) null);
	}

	public void subscribe(String inMethod, String outMethod) {
		subscribe(inMethod, outMethod,  (Class<?>[]) null);
	}

	public void subscribe(String outMethod, String inMethod, Class<?>... parameterType) {
		MRLListener listener = null;
		if (parameterType != null) {
			listener = new MRLListener(outMethod, myService.getName(), inMethod, parameterType);
		} else {
			listener = new MRLListener(outMethod, myService.getName(), inMethod, null);
		}

		myService.send(boundServiceName, "addListener", listener);

	}
	
	public void send(String method){
		send(method, (Object[])null);
	}
	
	public void send(String method, Object...params){
		myService.send(boundServiceName, method, params);
	}

	// TODO - more closely model java event system with addNotification or
	// addListener
	public void unsubscribe(String inOutMethod) {
		unsubscribe(inOutMethod, inOutMethod, (Class<?>[]) null);
	}

	public void unsubscribe(String inMethod, String outMethod) {
		unsubscribe(inMethod, outMethod, (Class<?>[]) null);
	}

	public void unsubscribe(String outMethod, String inMethod, Class<?>... parameterType) {

		MRLListener listener = null;
		if (parameterType != null) {
			listener = new MRLListener(outMethod, myService.getName(), inMethod, parameterType);
		} else {
			listener = new MRLListener(outMethod, myService.getName(), inMethod, null);
		}
		myService.send(boundServiceName, "removeListener", listener);

	}

	public abstract void attachGUI();

	public abstract void detachGUI();

	public int test(int i, double d) {
		int x = 0;
		return x;
	}

	public void makeReadyForRelease() {
	
	}

	public Component getTabControl() {
		return tabControl;
	}

}
