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

import org.myrobotlab.image.SerializableImage;
import org.myrobotlab.service.interfaces.GUI;

public class CalibratorGUI extends ServiceGUI {

	static final long serialVersionUID = 1L;
	// JList files;
	VideoWidget video = null;

	public CalibratorGUI(final String boundServiceName, final GUI myService) {
		super(boundServiceName, myService);
	}
	
	public void init() {
		video = new VideoWidget(boundServiceName, myService);
		video.init();
		gc.gridx = 0;
		gc.gridy = 0;
		display.add(video.display, gc);
	}

	public void webCamDisplay(SerializableImage img) {
		video.webCamDisplay(img);
	}

	@Override
	public void attachGUI() {
		video.attachGUI();
	}

	@Override
	public void detachGUI() {
		video.detachGUI();
	}

}
