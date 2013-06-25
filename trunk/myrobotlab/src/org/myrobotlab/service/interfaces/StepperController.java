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

package org.myrobotlab.service.interfaces;

import java.util.ArrayList;

import org.myrobotlab.service.data.Pin;

public interface StepperController {

	/**
	 * Remote attachment activation - used by services not in the same instance
	 * to attach a Motor to a MotorController
	 * 
	 * @param stepperName
	 * @param stepperData
	 */
	public boolean stepperAttach(String stepperName, Integer steps, Integer pin1, Integer pin2, Integer pin3, Integer pin4); 

	/**
	 * This is basic information to request from a Controller. A list of pins on
	 * the controller so GUIs or other services can figure out if there are any
	 * appropriate
	 * 
	 * @return
	 */
	public ArrayList<Pin> getPinList();

	/**
	 * moveTo - move the Motor a relative amount the amount can be negative or
	 * positive an integer value is expected
	 * 
	 * @param name
	 *            - name of the Motor
	 * @param position
	 *            - positive or negative absolute amount to move the Motor
	 * @return void
	 */
	public void stepperMoveTo(String name, Integer position);

	/**
	 * 
	 * request for stepper to move the stepper can be queried for the new powerlevel
	 * and the controller shall appropriately change power level and direction
	 * if necessary
	 * 
	 * @param name
	 */
	public void stepperMove(String name);

	/**
	 * MotorDetach - detach the Motor from a specific pin on the controller
	 * 
	 * @param name
	 *            - name of the Motor
	 * @return void
	 */
	public boolean stepperDetach(String name);

	public String getName();

	/**
	 * method to return stepper information
	 * 
	 * @param stepperName
	 * @return
	 */
	public Object[] getMotorData(String stepperName);

}
