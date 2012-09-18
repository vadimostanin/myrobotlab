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

package org.myrobotlab.service;

import java.util.TreeMap;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;

public class Joystick extends Service {

	private static final long serialVersionUID = 1L;
	
	  public static final int NUM_BUTTONS = 12;

	  // public stick and hat compass positions 
	  public static final int NUM_COMPASS_DIRS = 9;

	  public static final int NW = 0;
	  public static final int NORTH = 1;
	  public static final int NE = 2;
	  public static final int WEST = 3;
	  public static final int NONE = 4;   // default value
	  public static final int EAST = 5;
	  public static final int SW = 6;
	  public static final int SOUTH = 7;
	  public static final int SE = 8;
	  
	public final static Logger log = Logger.getLogger(Joystick.class.getCanonicalName());
	
	public final static String Z_AXIS = "Z_AXIS";
	public final static String Z_ROTATION = "Z_ROTATION";

	Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
	TreeMap<String, Integer> controllerNames = new TreeMap<String, Integer> ();
	
	InputPollingThread pollingThread = null;
	int myDeviceIndex = -1;
	Controller controller = null;
	double[] lastValues;
	
	public class InputPollingThread extends Thread
	{
		public boolean isPolling = false;
		
		public InputPollingThread(String name)
		{
			super(name);
		}
		
		
		public void run()
		{
			
			if (controller == null)
			{
				log.error("controller is null - can not poll");
			}
			
			/* Get all the axis and buttons */
			Component[] components = controller.getComponents();
			lastValues = new double[components.length];	
			
			isPolling = true;
			while (isPolling)
			{
				
					/* Poll the controller */
					controller.poll();

					StringBuffer buffer = new StringBuffer();

					/* For each component, get it's name, and it's current value */
					for (int i = 0; i < components.length; i++) {
						if (i > 0) {
							buffer.append(", ");
						}
						String n = components[i].getName();
						buffer.append(n);
						if ("Z Axis".equals(n))
						{
							double in = components[i].getPollData();
							int pos = (int)((90 * in) + 90);
							if (lastValues[i] != pos)
							{
								invoke("ZAxis", pos);
							}
							lastValues[i] = pos;

						} else if ("Z Rotation".equals(n))
						{
							double in = components[i].getPollData();
							int pos = (int)((90 * in) + 90);
							if (lastValues[i] != pos)
							{
								invoke("ZRotation", pos);
							}
							lastValues[i] = pos;
						}  else if ("X Axis".equals(n))
						{
							double in = components[i].getPollData();
							int pos = (int)((90 * in) + 90);
							if (lastValues[i] != pos)
							{
								invoke("XAxis", pos);
							}
							lastValues[i] = pos;

						}  else if ("X Axis".equals(n))
						{
							double in = components[i].getPollData();
							int pos = (int)((90 * in) + 90);
							if (lastValues[i] != pos)
							{
								invoke("XAxis", pos);
							}
							lastValues[i] = pos;

						}
						
						
						
						buffer.append(": ");
						if (components[i].isAnalog()) {
							/* Get the value at the last poll of this component */
							buffer.append(components[i].getPollData());
						} else {
							if (components[i].getPollData() == 1.0f) {
								buffer.append("On");
							} else {
								buffer.append("Off");
							}
						}
					}

					//log.debug(buffer.toString());
					/*
					 * Sleep for 20 millis, this is just so the example doesn't thrash
					 * the system.
					 * FIXME - can a polling system be avoided - could this block with 
					 * the JNI code?
					 */
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			}
		}
	
	public Joystick(String n) {
		super(n, Joystick.class.getCanonicalName());

		for (int i = 0; i < controllers.length; i++) {
			log.info(String.format("Found input device: %d %s", i, controllers[i].getName()));
			if (controllers[i].getType() == Controller.Type.GAMEPAD || controllers[i].getType() == Controller.Type.STICK)
			{
				controllerNames.put(String.format("%d - %s",i, controllers[i].getName()), i);
			}
			// search for gamepad or joystick
		}
	}
	
	public boolean attach(Servo servo, String axis)
	{
		if (Z_AXIS.equals(axis))
		{
			servo.subscribe("ZAxis", getName(), "moveTo", Integer.class);
			return true;
		} else if (Z_ROTATION.equals(axis)) 
		{
			servo.subscribe("ZRotation", getName(), "moveTo", Integer.class);	
			return true;
		}
		
		log.error(String.format("unknown axis %s", axis));
		return false;
	}

	//---------------Publishing Begin ------------------------
	public Integer ZAxis(Integer zaxis)
	{
		return zaxis;
	}

	public Integer ZRotation(Integer zaxis)
	{
		return zaxis;
	}
	//---------------Publishing End ------------------------
	
	
	public boolean setController(int index)
	{
		//if ()
		log.info(String.format("attaching controller %d", index));
		if (index > -1 && index < controllers.length) {
			controller = controllers[index];
			return true;
		}
		log.error("bad index");
		return false;
	}
	
	public boolean setController(String s)
	{
		if (controllerNames.containsKey(s))
		{
			setController(controllerNames.get(s));
			return true;
		}
		log.error(String.format("cant find %s", s));
		return false;
	}
	
	public void startPolling()
	{

		pollingThread = new InputPollingThread(String.format("%s_polling", getName()));
		pollingThread.start();
	}
	
	public void stopPolling()
	{
		if (pollingThread != null)
		{
			pollingThread.isPolling = false;
			pollingThread = null;
		}
	}

	@Override
	public void loadDefaultConfiguration() {
	}


	@Override
	public String getToolTip() {
		return "used for interfacing with a Joystick";
	}
	
	public TreeMap<String, Integer> getControllerNames()
	{
		return controllerNames;
	}

	public static void main(String args[]) {
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		// First you need to create controller.
		// http://theuzo007.wordpress.com/2012/09/02/joystick-in-java-with-jinput/
		//JInputJoystick joystick = new JInputJoystick(Controller.Type.STICK, Controller.Type.GAMEPAD);

		Joystick joy = new Joystick("joystick");
		joy.startService();
		//joy.setController(2);
		//joy.startPolling();	
		
		GUIService gui = new GUIService("gui");
		gui.startService();
		gui.display();
		
	}

}
