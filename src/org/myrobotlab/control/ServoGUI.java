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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.log4j.Logger;
import org.myrobotlab.framework.ConfigurationManager;
import org.myrobotlab.framework.RuntimeEnvironment;
import org.myrobotlab.service.Servo;
import org.myrobotlab.service.interfaces.GUI;

public class ServoGUI extends ServiceGUI {

	public final static Logger LOG = Logger.getLogger(ServoGUI.class.getCanonicalName());
	static final long serialVersionUID = 1L;

	JLabel boundPos = null;

	DigitalButton attachButton = null;
	JSlider slider = null;

	BasicArrowButton right = new BasicArrowButton(BasicArrowButton.EAST);
	BasicArrowButton left = new BasicArrowButton(BasicArrowButton.WEST);

	JComboBox controller = null;
	JComboBox pin = null;
	
	// TODO - sync initially by requesting entire Servo service object - can you get cfg? that way?
	JTextField posMin = new JTextField("0");
	JTextField posMax = new JTextField("180");
	
	Servo myServo = null;

	public ServoGUI(final String boundServiceName, final GUI myService) {
		super(boundServiceName, myService);
	}
	
	public void init() {

		left.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setValue(slider.getValue() - 1);
			}
		});
		right.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setValue(slider.getValue() + 1);
			}
		});

		// build input begin ------------------
		JPanel input = new JPanel();
		input.setLayout(new GridBagLayout());

		// row 1
		gc.gridx = 0;
		gc.gridy = 0;

		input.add(getAnalogValue(), gc);

		++gc.gridx;
		input.add(new JLabel(" "));
		++gc.gridx;
		boundPos = new JLabel("90");

		input.add(boundPos, gc);

		gc.gridwidth = 2;
		gc.gridx = 1;
		++gc.gridy;
		input.add(left, gc);
		++gc.gridx;

		input.add(right, gc);
		++gc.gridx;

		gc.gridx = 0;
		++gc.gridy;

		JPanel control = new JPanel();
		input.setLayout(new GridBagLayout());

		gc.gridx = 0;
		gc.gridy = 0;

		attachButton = new DigitalButton();
		attachButton.setText("attach");
		control.add(attachButton, gc);
		++gc.gridx;

		Vector<String> v = getAllServoControllers();
		controller = new JComboBox(v);
		control.add(controller, gc);

		++gc.gridx;
		control.add(new JLabel("pin"), gc); // TODO build pin arrangement for
											// Arduino - getValidPinsForServo in
											// Interface

		// TODO - pin config is based on Arduino D.
		Vector<Integer> p = new Vector<Integer>();
		p.addElement(1);
		p.addElement(2);
		p.addElement(3);
		p.addElement(4);
		p.addElement(5);
		p.addElement(6);
		p.addElement(7);
		p.addElement(8);
		p.addElement(9);
		p.addElement(10);
		p.addElement(11);
		p.addElement(12);
		p.addElement(13);

		pin = new JComboBox(p);

		++gc.gridx;
		control.add(pin, gc);

		display.add(control);
		display.add(input);
		
		gc.gridx = 0;
		++gc.gridy;

		JPanel limits = new JPanel();
		limits.add(new UpdateLimits());
		limits.add(new JLabel("min "));
		limits.add(posMin);
		limits.add(new JLabel(" max "));
		limits.add(posMax);

		display.add(limits, gc);

        myServo = (Servo)RuntimeEnvironment.getService(boundServiceName).service;
		
	}
	
	public void attachGUI() {
		/*
		 * ARRGH!! - BUG sendBLocking dangerous for GUI
		 * 
		 * // gui is querying servo - is it attached to a controller? boolean
		 * isAttached = (Boolean)myService.sendBlocking(boundServiceName,
		 * "isAttached", null);
		 * 
		 * if (isAttached) { attachButton.setText("detach"); // query servo for
		 * state data Integer servoPinValue =
		 * (Integer)myService.sendBlocking(boundServiceName, "getPin", null);
		 * String servoControllerName =
		 * (String)myService.sendBlocking(boundServiceName, "getControllerName",
		 * null); Integer servoPos =
		 * (Integer)myService.sendBlocking(boundServiceName, "getPos", null);
		 * slider.setValue(servoPos); pin.setSelectedItem(servoPinValue);
		 * controller.setSelectedItem(servoControllerName); } else {
		 * attachButton.setText("attach"); controller.setSelectedItem(""); }
		 */
		LOG.info(pin);
		// IOData d = (IOData)myService.sendBlocking(boundServiceName, "moveTo",
		// data);
		// removeNotifyRequest("getCFG", "setController",
		// String.class.getCanonicalName()); -- TODO - done in
	}

	private JSlider getAnalogValue() {
		if (slider == null) {
			slider = new JSlider(0, 180, 90);
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					boundPos.setText("" + slider.getValue());

					if (myService != null) {
						myService.send(boundServiceName, "moveTo", new Integer(
								slider.getValue()));
					} else {
						LOG.error("can not send message myService is null");
					}
				}
			});

		}
		return slider;
	}

	private class DigitalButton extends JButton implements ActionListener {
		private static final long serialVersionUID = 1L;

		public DigitalButton() {
			super();
			setText("Off");
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (getText().equals("attach")) {
				setText("detach");
				myService.send(boundServiceName, "attach", controller
						.getSelectedItem().toString(), pin.getSelectedItem());
			} else {
				setText("attach");
				myService.send(boundServiceName, "detach", null);
			}
		}
	}

	private class UpdateLimits extends JButton implements ActionListener {
		private static final long serialVersionUID = 1L;

		public UpdateLimits() {
			super();
			setText("update limits");
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			myService.send(boundServiceName, "setPosMin", Integer.parseInt(posMin.getText()));
			myService.send(boundServiceName, "setPosMax", Integer.parseInt(posMax.getText()));
		}
	}
	
	
	@Override
	public void detachGUI() {
		// TODO Auto-generated method stub

	}

	public Vector<String> getAllServoControllers() {
		Vector<String> v = new Vector<String>();
		v.addElement(""); // the "no interface" selection
		ConfigurationManager cm = myService.getHostCFG();
		Vector<String> sm = cm.getServiceVector();
		for (int i = 0; i < sm.size(); ++i) {
			Vector<String> intfcs = cm.getInterfaces(sm.get(i));
			if (intfcs == null)
				continue;
			for (int j = 0; j < intfcs.size(); ++j) {
				if (intfcs.get(j).compareTo(
						"org.myrobotlab.service.interfaces.ServoController") == 0) {
					v.addElement(sm.get(i));
				}
			}

		}
		return v;
	}
	
	public void getState(Servo servo)
	{
	}

}