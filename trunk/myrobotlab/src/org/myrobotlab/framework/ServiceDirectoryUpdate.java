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

package org.myrobotlab.framework;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class ServiceDirectoryUpdate implements Serializable {
	private static final long serialVersionUID = 1L;
	public final static Logger LOG = Logger.getLogger(ServiceDirectoryUpdate.class);

	public int ID;
	public URL url; 
	/**
	 * URL built by process which accepted the communication endpoint. The
	 * receiving communication adapter provides the information to build this
	 * URL
	 */
	public URL remoteURL;
	/**
	 * unused
	 */
	public String type;
	public int servicePort;
	/**
	 * The list of services to be registered on the targe MRL instance.
	 */
	public ServiceEnvironment serviceEnvironment;
	public ArrayList<Property> propertyList; // operator name the sdu came from

}