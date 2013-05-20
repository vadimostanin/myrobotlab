from java.lang import String
from java.lang import Class
from java.awt import Rectangle
from org.myrobotlab.service import Runtime
from org.myrobotlab.service import OpenCV
from org.myrobotlab.opencv import OpenCVData
from com.googlecode.javacv.cpp.opencv_core import CvPoint;
from org.myrobotlab.service import OpenCV
from org.myrobotlab.service import Arduino
from org.myrobotlab.service import Servo
from time import sleep
 
 
tracker = Runtime.create("tracker","Tracking")
 
# create all the peer services
rotation = Runtime.create("rotation","Servo")
neck = Runtime.create("neck","Servo")
arduino = Runtime.create("arduino","Arduino")
xpid = Runtime.create("xpid","PID");
ypid = Runtime.create("ypid","PID");
 
# adjust values
arduino.setSerialDevice("COM3")
eye = Runtime.create("eye","OpenCV")
opencv = Runtime.create("opencv","OpenCV")
opencv.startService()
opencv.setCameraIndex(1)
eye.setCameraIndex(1)
 
# flip the pid if needed
#xpid.invert()
xpid.setOutputRange(-3, 3)
xpid.setPID(10.0, 5, 1.0)
xpid.setSetpoint(0.5) # we want the target in the middle of the x
 
# flip the pid if needed
#ypid.invert()
ypid.setOutputRange(-3, 3)
ypid.setPID(10.0, 5, 1.0)
ypid.setSetpoint(0.5)
 
# set safety limits - servos
# will not go beyond these limits
rotation.setPositionMin(50)
rotation.setPositionMax(170)
 
neck.setPositionMin(50)
neck.setPositionMax(170)
 
# here we are attaching to the
# manually created peer services
 
tracker.attach(arduino)
tracker.attachServos(rotation, 3, neck, 9)
tracker.attach(eye)
tracker.attachPIDs(xpid, ypid)
 
tracker.setRestPosition(90, 72)
 
tracker.startService()
tracker.stopVideoStream()
tracker.trackPoint(0.5,0.5)

opencv.addFilter("pd1", "PyramidDown")
opencv.addFilter("FaceDetect1", "FaceDetect")
 
def input():
 
    #print 'found face at (x,y) ', msg_opencv_publishOpenCVData.data[0].x(), msg_opencv_publish.data[0].y()
    opencvData = msg_opencv_publishOpenCVData.data[0]
    global x
    global y
    global sposx
    global sposy
    global posx
    global posy
    global newx
    global newy
    if (opencvData.getBoundingBoxArray().size() > 0) :
     rect = opencvData.getBoundingBoxArray().get(0)
     posx = rect.x
     posy = rect.y
     
     w = rect.width
     h = rect.height
     sposx = (w/2)
     sposy = (h/2)
     x = (posx + sposx)
     y = (posy + sposy)
     print x
     print y
     newx = (x/320.0)
     newy = (y/240.0)
     print newx
     print newy
     opencv.removeFilter("FaceDetect1")
     tracker.stopVideoStream()
     tracker.trackPoint(newx,newy)
     return object
# create a message route from opencv to python so we can see the coordinate locations
opencv.addListener("publishOpenCVData", python.name, "input", OpenCVData().getClass());

opencv.capture()