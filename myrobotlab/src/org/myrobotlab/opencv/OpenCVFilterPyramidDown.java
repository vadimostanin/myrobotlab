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

package org.myrobotlab.opencv;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvPyrDown;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
import org.myrobotlab.service.OpenCV;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class OpenCVFilterPyramidDown extends OpenCVFilter {

	private static final long serialVersionUID = 1L;

	public final static Logger log = Logger.getLogger(OpenCVFilterPyramidDown.class.getCanonicalName());

	final static int CV_GAUSSIAN_5X5 = 7;

	transient IplImage dst = null;
	transient BufferedImage frameBuffer = null;
	
	public OpenCVFilterPyramidDown(OpenCV service, String name) {
		super(service, name);
	}
	
	@Override
	public BufferedImage display(IplImage image, Object[] data) {

		return dst.getBufferedImage();
	}


	@Override
	public IplImage process(IplImage image) {

		cvPyrDown(image, dst, CV_GAUSSIAN_5X5);

		return dst;
	}

	@Override
	public void imageChanged(IplImage frame) {
		dst = cvCreateImage(cvSize(frame.width() / 2, frame.height() / 2), frame.depth(), frame.nChannels());
	}

}