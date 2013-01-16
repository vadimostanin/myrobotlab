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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.myrobotlab.control.widget.JIntegerField;
import org.myrobotlab.service.Clock;
import org.myrobotlab.service.Clock.PulseDataType;
import org.myrobotlab.service.Runtime;
import org.myrobotlab.service.interfaces.GUI;

public class ClockGUI extends ServiceGUI implements ActionListener {

	static final long serialVersionUID = 1L;
	JButton startClock = new JButton("start clock");
	JButton startCountDown = new JButton("start count down");

	JPanel clockDisplayPanel = new JPanel(new BorderLayout());
	JPanel clockControlPanel = new JPanel();

	JLabel clockDisplay = new JLabel("<html><p style=\"font-size:30px;\">00:00:00</p></html>");
	JLabel msgDisplay = new JLabel("");

	ButtonGroup group = new ButtonGroup();

	Date countDownTo = null;

	JRadioButton none = new JRadioButton("none");
	JRadioButton increment = new JRadioButton("increment");
	JRadioButton integer = new JRadioButton("integer");
	JRadioButton string = new JRadioButton("string");

	JTextField interval = new JTextField("1000");
	JTextField pulseDataString = new JTextField(10);
	JIntegerField pulseDataInteger = new JIntegerField(10);

	Clock myClock = null;

	public ClockGUI(final String boundServiceName, final GUI myService) {
		super(boundServiceName, myService);
	}

	public void init() {
		display.setLayout(new BorderLayout());

		clockDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		clockDisplayPanel.add(clockDisplay, BorderLayout.CENTER);
		msgDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		clockDisplayPanel.add(msgDisplay, BorderLayout.SOUTH);

		display.add(clockDisplayPanel, BorderLayout.CENTER);
		display.add(clockControlPanel, BorderLayout.SOUTH);

		startClock.addActionListener(this);
		startCountDown.addActionListener(this);
		clockControlPanel.add(startCountDown);
		clockControlPanel.add(startClock);
		clockControlPanel.add(new JLabel("  interval  "));
		clockControlPanel.add(interval);
		clockControlPanel.add(new JLabel("  ms  "));

		// build filters begin ------------------
		JPanel pulseData = new JPanel(new GridBagLayout());
		TitledBorder title;
		title = BorderFactory.createTitledBorder("pulse data");
		pulseData.setBorder(title);

		none.setActionCommand("none");
		none.setSelected(true);
		none.addActionListener(this);

		increment.setActionCommand("increment");
		increment.addActionListener(this);

		integer.setActionCommand("integer");
		integer.addActionListener(this);

		string.setActionCommand("string");
		string.addActionListener(this);

		// Group the radio buttons.
		group.add(none);
		group.add(increment);
		group.add(integer);
		group.add(string);

		pulseData.add(none);
		pulseData.add(increment);
		pulseData.add(integer);
		pulseData.add(pulseDataInteger);
		pulseData.add(string);
		pulseData.add(pulseDataString);
		clockControlPanel.add(pulseData);

		myClock = (Clock) Runtime.getServiceWrapper(boundServiceName).service;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == none || o == increment || o == integer || o == string) {
			myClock.setType(((JRadioButton) e.getSource()).getText());
		}

		if (o == startClock) {
			if (startClock.getText().compareTo("start clock") == 0) {
				startClock.setText("stop clock");

				myClock.interval = Integer.parseInt(interval.getText());
				myClock.pulseDataInteger = Integer.parseInt(pulseDataInteger.getText());
				myClock.pulseDataString = pulseDataString.getText();

				// set the state of the bound service - whether local or remote
				myService.send(boundServiceName, "setState", myClock); // double
																		// set
																		// on
																		// local
				// publish the fact you set the state -
				// TODO - should this a function which calls both functions ?
				myService.send(boundServiceName, "publishState"); // TODO - bury
																	// in
																	// Service.SetState?
				myService.send(boundServiceName, "startClock");

			} else {
				startClock.setText("start clock");
				myService.send(boundServiceName, "stopClock");
			}
		}

		if (o == startCountDown) {

			countDownTo = Clock.getFutureDate(5, 0);
			myService.send(boundServiceName, "startCountDown", countDownTo);
		}
	}

	// FIXME - is get/set state interact with Runtime registry ???
	// it probably should
	public void getState(Clock c) {
		// Setting the clockControlPanel fields based on incoming Clock data
		// if the Clock is local - the actual clock is sent
		// if the Clock is remote - a data proxy is sent
		if (c != null) {
			if (c.pulseDataType == PulseDataType.increment) {
				increment.setSelected(true);
			} else if (c.pulseDataType == PulseDataType.integer) {
				integer.setSelected(true);

			} else if (c.pulseDataType == PulseDataType.string) {
				string.setSelected(true);

			} else if (c.pulseDataType == PulseDataType.none) {
				none.setSelected(true);
			}

			pulseDataString.setText(c.pulseDataString);

			pulseDataInteger.setInt(c.pulseDataInteger);

			interval.setText((c.interval + ""));

			if (c.isClockRunning) {
				startClock.setText("stop clock");
			} else {
				startClock.setText("start clock");
			}

		}

	}

	String displayFormat = "<html><p style=\"font-size:30px\">%02d:%02d:%02d</p></html>";

	public void countdown(Long amtRemaining) {

		long sec = amtRemaining / 1000 % 60;
		long min = amtRemaining / (60 * 1000) % 60;
		long hrs = amtRemaining / (60 * 60 * 1000) % 12;

		//clockControlPanel.setVisible(false);
		// color:#2BFF00;
		//msgDisplay.setText("<html><p style=\"font-size:10px;text-align:center;\">until core meltdown<br/>have a nice day !</p></html>");
		msgDisplay.setText("");
		clockDisplay.setOpaque(true);
		msgDisplay.setOpaque(true);
		clockDisplay.setBackground(new Color(0x2BFF00));
		msgDisplay.setBackground(new Color(0x2BFF00));

		clockDisplay.setText(String.format(displayFormat, hrs, min, sec));
	}

	public void addClockEvent(Date time, String name, String method, Object... data) {
		myService.send(boundServiceName, "addClockEvent", time, name, method, data);
	}

	// FIXME sendNotifyStateRequest("publishState", "getState", String type); <-
	// Class.forName(type)
	@Override
	public void attachGUI() {
		subscribe("countdown", "countdown", Long.class);
		subscribe("publishState", "getState", Clock.class);
		subscribe("pulse", "pulse");
		
		myService.send(boundServiceName, "publishState");
	}

	@Override
	public void detachGUI() {
		unsubscribe("countdown", "countdown", Long.class);
		unsubscribe("publishState", "getState", Clock.class);
		unsubscribe("pulse", "pulse");
	}
	
	public void pulse()
	{
		countdown(System.currentTimeMillis());
	}

}
