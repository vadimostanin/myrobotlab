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
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.OpenCV;
import org.slf4j.Logger;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class OpenCVFilterGray extends OpenCVFilter {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(OpenCVFilterGray.class.getCanonicalName());

	IplImage gray = null;
	IplImage color = null;

	public OpenCVFilterGray(VideoProcessor vp, String name, HashMap<String, IplImage> source,  String sourceKey)  {
		super(vp, name, source, sourceKey);
	}

	@Override
	public BufferedImage display(IplImage image) {
		return image.getBufferedImage();
	}

	@Override
	public IplImage process(IplImage image, OpenCVData data) {

		// what can you expect? nothing? - if data != null then error?
		if (image.nChannels() == 3) {
			if (gray == null) {
				
			}
			cvCvtColor(image, gray, CV_BGR2GRAY);
		} else if (image.nChannels() == 1) {
			if (color == null) {
				int depth = image.depth();
				color = cvCreateImage(cvGetSize(image), depth, 3);
			}
			cvCvtColor(image, gray, CV_GRAY2BGR);
		}

		return gray;
	}

	@Override
	public void imageChanged(IplImage image) {
		gray = cvCreateImage(cvGetSize(image), 8, 1);
	}

}