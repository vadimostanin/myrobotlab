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
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
import org.myrobotlab.service.OpenCV;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;



public class OpenCVFilterCanny extends OpenCVFilter {

	private static final long serialVersionUID = 1L;
	
	public final static Logger log = Logger.getLogger(OpenCVFilterCanny.class
			.getCanonicalName());

	IplImage gray = null;
	IplImage inlines = null;
	BufferedImage frameBuffer = null;
	//double lowThreshold = 0.0;
	//double highThreshold = 50.0;
	int apertureSize = 0;

	public OpenCVFilterCanny(OpenCV service, String name) {
		super(service, name);
	}

	@Override
	public BufferedImage display(IplImage image, Object[] data) {

		frameBuffer = inlines.getBufferedImage(); 														
		return frameBuffer;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void loadDefaultConfiguration() {
		cfg.set("lowThreshold", 0.0f);
		cfg.set("highThreshold", 50.0f);
		cfg.set("apertureSize", 5);

	}

	CvPoint p0 = new CvPoint(0, 0);
	CvPoint p1 = new CvPoint(0, 0);

	@Override
	public IplImage process(IplImage image) {

		if (image == null) {
			log.error("image is null");
		}

		if (gray == null) {
			gray = cvCreateImage(cvGetSize(image), 8, 1);
		}
		if (inlines == null) {
			inlines = cvCreateImage(cvGetSize(image), 8, 1);
		}

		if (image.nChannels() == 3) {
			cvCvtColor(image, gray, CV_BGR2GRAY);
		} else {
			gray = image.clone();
		}
		/*
		 * lowThreshold = 600.0; highThreshold = 1220.0; apertureSize = 5;
		 */
		//lowThreshold = 90.0;
		//highThreshold = 110.0;
		//apertureSize = 3;
		apertureSize = cfg.getInt("apertureSize"); 
		cvCanny(gray, inlines, cfg.getFloat("lowThreshold"),  cfg.getFloat("highThreshold"), cfg.getInt("apertureSize") );

		return inlines;
	}

}