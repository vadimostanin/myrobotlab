package org.myrobotlab.service;

import java.util.HashMap;

import org.myrobotlab.framework.Peers;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.Status;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.service.interfaces.ServiceInterface;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;

public class InMoov extends Service {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(InMoov.class);

	// OBJECTIVE - try only have complex composite interaction here - everything
	// else should be done directly to targeted services !!!
	// OBJECTIVE - always return a service !!!

	// port map NOT SURE ????
	// will no right & left and com ports
	// 3 definitions at the top left right and head
	// port index, local references
	// HashMap <String, Arduino> arduinos = new HashMap <String, Arduino>();

	// services which do not require a body part
	// or can influence multiple body parts

	// Dynamic reflective services such as WebGui & XMPP are to be left out of
	// Peer definitions

	@Element(required = false)
	String defaultLeftPort;

	@Element(required = false)
	String defaultRightPort;

	// hands and arms
	transient public InMoovHead head;
	transient public InMoovArm leftArm;
	transient public InMoovHand leftHand;
	transient public InMoovArm rightArm;
	transient public InMoovHand rightHand;

	public final String left = "left";
	public final String right = "right";

	transient private HashMap<String, InMoovArm> arms = new HashMap<String, InMoovArm>();
	transient private HashMap<String, InMoovHand> hands = new HashMap<String, InMoovHand>();

	// peers
	transient public Sphinx ear;
	transient public Speech mouth;
	transient public Tracking eyesTracking;
	transient public Tracking headTracking;
	transient public OpenCV opencv;
	transient public MouthControl mouthControl;

	// reflective or non-interactive peers
	// transient public WebGUI webgui;
	// transient public XMPP xmpp;
	// transient public Security security;

	// static in Java are not overloaded but overwritten - there is no
	// polymorphism for statics
	public static Peers getPeers(String name) {
		Peers peers = new Peers(name);

		// SHARING !!!
		peers.suggestAs("head.arduino", "left", "Arduino", "shared left arduino");

		peers.suggestAs("leftArm.arduino", "left", "Arduino", "shared left arduino");
		peers.suggestAs("leftHand.arduino", "left", "Arduino", "shared left arduino");

		peers.suggestAs("rightArm.arduino", "right", "Arduino", "shared right arduino");
		peers.suggestAs("rightHand.arduino", "right", "Arduino", "shared right arduino");

		peers.suggestAs("eyesTracking.opencv", "opencv", "OpenCV", "shared head OpenCV");
		peers.suggestAs("eyesTracking.arduino", "left", "Arduino", "shared head Arduino");
		peers.suggestAs("eyesTracking.x", "head.eyeX", "Servo", "shared servo");
		peers.suggestAs("eyesTracking.y", "head.eyeY", "Servo", "shared servo");

		peers.suggestAs("headTracking.opencv", "opencv", "OpenCV", "shared head OpenCV");
		peers.suggestAs("headTracking.arduino", "left", "Arduino", "shared head Arduino");
		peers.suggestAs("headTracking.x", "head.rothead", "Servo", "shared servo");
		peers.suggestAs("headTracking.y", "head.neck", "Servo", "shared servo");

		peers.suggestAs("mouthControl.arduino", "left", "Arduino", "shared head Arduino");
		peers.suggestAs("mouthControl.mouth", "mouth", "Speech", "shared Speech");
		peers.suggestAs("mouthControl.jaw", "head.jaw", "Servo", "shared servo");

		// put peer definitions in
		peers.put("leftArm", "InMoovArm", "left arm");
		peers.put("leftHand", "InMoovHand", "left hand");
		peers.put("rightArm", "InMoovArm", "right arm");
		peers.put("rightHand", "InMoovHand", "right hand");

		peers.put("ear", "Sphinx", "InMoov spech recognition service");
		peers.put("eyesTracking", "Tracking", "Tracking for the eyes");
		peers.put("head", "InMoovHead", "the head");
		peers.put("headTracking", "Tracking", "Head tracking system");
		peers.put("mouth", "Speech", "InMoov speech service");
		peers.put("mouthControl", "MouthControl", "MouthControl");
		peers.put("opencv", "OpenCV", "InMoov OpenCV service");

		return peers;
	}

	public InMoov(String n) {
		super(n);
		addRoutes();
	}
	
	boolean speakErrors = true;
	
	public boolean speakErrors(boolean b){
		speakErrors = b;
		return b;
	}

	public void addRoutes() {
		// register with runtime for any new services
		// their errors are routed to mouth
		subscribe(this.getName(), "publishError", "handleError");

		Runtime r = Runtime.getInstance();
		r.addListener(getName(), "registered");
	}

	/**
	 * Service registration event. On newly registered service the InMoov
	 * service will set up various routing.
	 * 
	 * Routing of errors back to the InMoov service. This will allow the mouth
	 * to announce errors
	 * 
	 * @param sw
	 */
	public void registered(ServiceInterface sw) {
		// FIXME FIXME FIXME !!! - this right idea - but expanded methods have
		// incorrect parameter placement !!
		// addListener & suscribe the same !!!!
		subscribe(sw.getName(), "publishError", "handleError");
	}

	String lastError = "";
	public void handleError(String msg) {
		// lets try not to nag
		if (!lastError.equals(msg) && speakErrors){
			speakBlocking(msg);
		}
		lastError = msg;
	}

	/************* STARTS BEGIN ************************/

	public void startAll(String leftPort, String rightPort) {
		// TODO add vision
		startMouth();
		startHead(leftPort);
		startEar();
		startLeftHand(leftPort);
		startRightHand(rightPort);
		startLeftArm(leftPort);
		startRightArm(rightPort);

		startHeadTracking(leftPort);
		startEyesTracking(leftPort);

		speakBlocking("startup sequence completed");
	}

	// TODO TODO TODO - context & status report -
	// "current context is right hand"
	// FIXME - voice control for all levels (ie just a hand or head !!!!)
	public Sphinx startEar() {
		speakBlocking("starting ear");

		ear = (Sphinx) startPeer("ear");
		ear.addCommand("rest", getName(), "rest");
		ear.addCommand("attach", getName(), "attach");
		ear.addCommand("detach", getName(), "detach");
		ear.addCommand("open hand", getName(), "bothHandsOpen");
		ear.addCommand("close hand", getName(), "handClose", "both");

		ear.addCommand("camera on", getName(), "cameraOn");
		ear.addCommand("off camera", getName(), "cameraOff");

		ear.addCommand("capture gesture", getName(), "captureGesture"); // TODO

		ear.addCommand("track", getName(), "track");
		ear.addCommand("freeze track", getName(), "stopTracking");

		ear.addCommand("manual", ear.getName(), "lockOutAllGrammarExcept", "voice control"); // important

		// ear.addCommand("hello", getName(), "hello");
		ear.addCommand("hello", "python", "hello"); // wrong

		ear.addComfirmations("yes", "correct", "ya");
		ear.addNegations("no", "wrong", "nope", "nah");

		// ear.startListening();
		return ear;
	}

	public Speech startMouth() {
		mouth = (Speech) startPeer("mouth");
		speakBlocking("starting mouth");

		if (ear != null) {
			ear.attach(mouth);
		}
		return mouth;
	}

	// starting routines need to be fully re-entrant
	// they can be used to get a reference and start a very limited sub-system
	// of inmoov
	// very useful in the fact a head subsystem can be tested without starting
	// all of the peer services of the head
	public OpenCV startOpenCV() {
		opencv = (OpenCV) startPeer("opencv");
		return opencv;
	}

	// NOTE - BEST Services are one which are reflective on startService
	// like xmpp which exposes a the reflective REST API are startService
	public Tracking startHeadTracking(String port) {
		speakBlocking("starting head tracking");

		if (head == null) {
			startHead(port);
		}
		headTracking = (Tracking) startPeer("headTracking");
		headTracking.connect(port);
		// TODO - test- report errors
		return headTracking;
	}

	public Tracking startEyesTracking(String port) {
		speakBlocking("starting eyes tracking");

		if (head == null) {
			startHead(port);
		}
		eyesTracking = (Tracking) startPeer("eyesTracking");
		eyesTracking.connect(port);
		return eyesTracking;
	}

	public MouthControl startMouthControl(String port) {
		mouthControl = (MouthControl) startPeer("mouthControl");
		mouthControl.jaw.setPin(26);
		mouthControl.arduino.connect(port);
		mouthControl.setmouth(10, 50);
		return mouthControl;
	}

	public InMoovHand startRightHand(String port) {
		return startRightHand(port, null);
	}

	public InMoovHand startRightHand(String port, String type) {
		rightHand = startHand(right, port, type);
		return rightHand;
	}

	public InMoovHand startLeftHand(String port) {
		return startLeftHand(port, null);
	}

	public InMoovHand startLeftHand(String port, String type) {
		leftHand = startHand(left, port, type);
		return leftHand;
	}

	public InMoovHand startHand(String side, String port, String boardType) {
		speakBlocking("starting %s hand", side);

		InMoovHand hand = (InMoovHand) createPeer(String.format("%sHand", side));
		hand.setSide(side);
		hands.put(side, hand);

		hand.arduino.setBoard(getBoardType(side, boardType));

		hand.connect(port);

		speakBlocking("testing %s hand", side);
		hand.test();

		return hand;
	}

	public InMoovArm startRightArm(String port) {
		return startRightArm(port, null);
	}

	public InMoovArm startRightArm(String port, String type) {
		rightArm = startArm(right, port, type);
		return rightArm;
	}

	public InMoovArm startLeftArm(String port) {
		return startLeftArm(port, null);
	}

	public InMoovArm startLeftArm(String port, String type) {
		leftArm = startArm(left, port, type);
		return leftArm;
	}
	
	String getBoardType(String side, String type){
		if (type != null){
			return type;
		}
		
		if (right.equals(side)){
			return Arduino.BOARD_TYPE_UNO;
		}
		
		return Arduino.BOARD_TYPE_ATMEGA2560;
	}

	public InMoovArm startArm(String side, String port, String boardType) {
		speakBlocking("starting %s arm", side);

		InMoovArm arm = (InMoovArm) createPeer(String.format("%sArm", side));
		arms.put(side, arm);
		arm.setSide(side);//FIXME WHO USES SIDE - THIS SHOULD BE NAME !!!
		arm.arduino.setBoard(getBoardType(side, boardType));
		arm.connect(port);

		speakBlocking("testing %s arm", side);
		arm.test();	

		return arm;
	}

	public void speakBlocking(Status test) {
		if (test != null){
			speakBlocking(test.toString());
		}
	}

	public InMoovHead startHead(String port) {

		opencv = (OpenCV) startPeer("opencv");
		return startHead(port, null);
	}

	public InMoovHead startHead(String port, String type) {
		// log.warn(InMoov.buildDNA(myKey, serviceClass))
		speakBlocking("starting head on %s", port);
		head = (InMoovHead) createPeer("head");

		if (type == null) {
			type = Arduino.BOARD_TYPE_ATMEGA2560;
		}

		head.arduino.setBoard(type);
		head.connect(port);

		speakBlocking("testing head");
		head.test();
		
		return head;
	}

	// ------ starts end ---------
	// ------ composites begin ---------

	@Override
	public String getDescription() {
		return "The InMoov service";
	}

	public void stopTracking() {
		if (eyesTracking != null) {
			eyesTracking.stopTracking();
		}

		if (headTracking != null) {
			headTracking.stopTracking();
		}
	}

	boolean speakBlocking(String speak, Object... fdata) {
		if (mouth != null) {
			return mouth.speakBlocking(speak, fdata);
		}

		return false;
	}

	boolean speakBlocking(String toSpeak) {
		if (mouth != null) {
			return mouth.speakBlocking(toSpeak);
		}
		return false;
	}

	// ------ composites end

	// ------ composites servos begin -----------

	public void fullSpeed() {
		if (head != null) {
			head.setSpeed(1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (rightHand != null) {
			rightHand.setSpeed(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (leftHand != null) {
			leftHand.setSpeed(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (rightArm != null) {
			rightArm.setSpeed(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (leftArm != null) {
			leftArm.setSpeed(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	public void rest() {
		if (head != null) {
			head.rest();
		}
		if (rightHand != null) {
			rightHand.rest();
		}
		if (leftHand != null) {
			leftHand.rest();
		}
		if (rightArm != null) {
			rightArm.rest();
		}
		if (leftArm != null) {
			leftArm.rest();
		}
	}

	public void detach() {
		if (head != null) {
			head.detach();
		}
		if (rightHand != null) {
			rightHand.detach();
		}
		if (leftHand != null) {
			leftHand.detach();
		}
		if (rightArm != null) {
			rightArm.detach();
		}
		if (leftArm != null) {
			leftArm.detach();
		}
	}

	public void attach() {
		if (head != null) {
			head.attach();
		}
		if (rightHand != null) {
			rightHand.attach();
		}
		if (leftHand != null) {
			leftHand.attach();
		}
		if (rightArm != null) {
			rightArm.attach();
		}
		if (leftArm != null) {
			leftArm.attach();
		}
	}

	// This is an in-flight check vs a startup or shutdown
	public void systemCheck() {
		speakBlocking("starting system check");
		speakBlocking("testing");

		rest();
		sleep(500);

		// FIXME !!!! set to mout = speak errors & warnings !!!!

		if (rightHand != null) {
			rightHand.test();
		}

		sleep(500);
		rest();

		// check servos

		// check ear

		// check mount - all my circuits are functioning perfectly

		broadcastState();
		speakBlocking("system check completed");
	}

	public void broadcastState() {
		if (leftHand != null) {
			leftHand.broadcastState();
		}

		if (rightHand != null) {
			rightHand.broadcastState();
		}

		if (leftArm != null) {
			leftArm.broadcastState();
		}

		if (rightArm != null) {
			rightArm.broadcastState();
		}

		if (head != null) {
			head.broadcastState();
		}
	}

	// ------ composites servos end -----------

	// ---------- canned gestures begin ---------
	public void bothHandsOpen() {
		moveHand(left, 0, 0, 0, 0, 0);
		moveHand(right, 0, 0, 0, 0, 0);
	}

	public void bothHandsClose() {
		moveHand(left, 130, 180, 180, 180, 180);
		moveHand(right, 130, 180, 180, 180, 180);
	}

	public void delicategrab() {
		setHandSpeed("left", 0.60f, 0.60f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.75f);
		moveHead(21, 98);
		moveArm("left", 30, 72, 77, 10);
		moveArm("right", 0, 91, 28, 17);
		moveHand("left", 131, 130, 4, 0, 0, 180);
		moveHand("right", 86, 51, 133, 162, 153, 180);
	}

	public void perfect() {
		setHandSpeed("left", 0.80f, 0.80f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 0.85f, 0.85f, 0.85f, 0.95f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.75f);
		moveHead(88, 79);
		moveArm("left", 89, 75, 93, 11);
		moveArm("right", 0, 91, 28, 17);
		moveHand("left", 120, 130, 60, 40, 0, 34);
		moveHand("right", 86, 51, 133, 162, 153, 180);
	}

	public void releasedelicate() {
		setHandSpeed("left", 0.60f, 0.60f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 0.75f, 0.75f, 0.75f, 0.95f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.75f);
		moveHead(20, 98);
		moveArm("left", 30, 72, 64, 10);
		moveArm("right", 0, 91, 28, 17);
		moveHand("left", 101, 74, 66, 58, 44, 180);
		moveHand("right", 86, 51, 133, 162, 153, 180);
	}

	public void grabthebottle() {
		setHandSpeed("left", 1.0f, 0.80f, 0.80f, 0.80f, 1.0f, 0.80f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.90f, 0.80f);
		moveHead(20, 88);
		moveArm("left", 77, 85, 45, 15);
		moveArm("right", 0, 90, 30, 10);
		moveHand("left", 109, 138, 180, 109, 180, 93);
		moveHand("right", 0, 0, 0, 0, 0, 90);
	}

	public void grabtheglass() {
		setHandSpeed("left", 0.60f, 0.60f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 0.60f, 0.60f, 1.0f, 1.0f, 0.70f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(20, 68);
		moveArm("left", 77, 85, 45, 15);
		moveArm("right", 48, 91, 72, 10);
		moveHand("left", 109, 138, 180, 109, 180, 93);
		moveHand("right", 140, 95, 100, 105, 143, 90);
	}

	public void poorbottle() {
		setHandSpeed("left", 0.60f, 0.60f, 0.60f, 0.60f, 0.60f, 0.60f);
		setHandSpeed("right", 0.60f, 0.80f, 0.60f, 0.60f, 0.60f, 0.60f);
		setArmSpeed("left", 0.60f, 0.60f, 0.60f, 0.60f);
		setArmSpeed("right", 0.60f, 0.60f, 0.60f, 0.60f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(20, 84);
		moveArm("left", 58, 40, 95, 30);
		moveArm("right", 68, 74, 43, 10);
		moveHand("left", 109, 138, 180, 109, 180, 4);
		moveHand("right", 145, 95, 110, 105, 143, 90);
	}

	public void givetheglass() {
		setHandSpeed("left", 0.60f, 0.60f, 0.60f, 0.60f, 0.60f, 0.60f);
		setHandSpeed("right", 0.60f, 0.80f, 0.60f, 0.60f, 0.60f, 0.60f);
		setArmSpeed("left", 0.60f, 0.60f, 0.60f, 0.60f);
		setArmSpeed("right", 0.60f, 0.60f, 0.60f, 0.60f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(47, 79);
		moveArm("left", 77, 75, 45, 17);
		moveArm("right", 21, 80, 77, 10);
		moveHand("left", 109, 138, 180, 109, 180, 93);
		moveHand("right", 102, 41, 72, 105, 143, 90);
	}

	public void takeball() {
		setHandSpeed("right", 0.75f, 0.75f, 0.75f, 0.75f, 0.85f, 0.75f);
		setArmSpeed("right", 0.85f, 0.85f, 0.85f, 0.85f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(30, 70);
		moveArm("left", 0, 84, 16, 15);
		moveArm("right", 6, 73, 76, 16);
		moveHand("left", 50, 50, 40, 20, 20, 90);
		moveHand("right", 150, 153, 153, 153, 153, 11);
	}

	public void keepball() {
		setHandSpeed("left", 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 1.0f);
		setHandSpeed("right", 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.75f, 0.85f, 0.95f, 0.85f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(20, 70);
		moveArm("left", 0, 84, 16, 15);
		moveArm("right", 54, 77, 55, 16);
		moveHand("left", 50, 65, 80, 46, 74, 90);
		moveHand("right", 40, 40, 40, 106, 180, 0);
	}

	public void approachlefthand() {
		setHandSpeed("right", 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.65f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.25f, 0.25f, 0.25f, 0.25f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(20, 70);
		moveArm("left", 90, 52, 59, 23);
		moveArm("right", 54, 77, 55, 16);
		moveHand("left", 50, 28, 30, 10, 10, 15);
		moveHand("right", 30, 30, 30, 106, 180, 0);
	}

	public void uselefthand() {
		setHandSpeed("right", 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.65f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.25f, 0.25f, 0.25f, 0.25f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(18, 84);
		moveArm("left", 90, 52, 59, 23);
		moveArm("right", 60, 64, 55, 16);
		moveHand("left", 50, 28, 30, 10, 10, 15);
		moveHand("right", 20, 20, 20, 106, 180, 0);
	}

	public void more() {
		setHandSpeed("right", 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.65f);
		setArmSpeed("left", 0.85f, 0.85f, 0.85f, 0.95f);
		setArmSpeed("right", 0.75f, 0.65f, 0.65f, 0.65f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(16, 84);
		moveArm("left", 90, 52, 59, 23);
		moveArm("right", 65, 56, 59, 16);
		moveHand("left", 145, 75, 148, 85, 10, 15);
		moveHand("right", 20, 20, 20, 106, 180, 0);
	}

	public void handdown() {
		setHandSpeed("left", 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f);
		setHandSpeed("right", 0.70f, 0.70f, 0.70f, 0.70f, 0.70f, 1.0f);
		moveHead(16, 84);
		moveArm("left", 90, 52, 59, 23);
		moveArm("right", 39, 56, 59, 16);
		moveHand("left", 145, 75, 148, 85, 10, 15);
		moveHand("right", 103, 66, 84, 106, 180, 0);
	}

	public void isitaball() {
		setHandSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.75f, 0.85f, 0.90f, 0.85f);
		setHeadSpeed(0.65f, 0.75f);
		moveHead(65, 74);
		moveArm("left", 70, 64, 87, 15);
		moveArm("right", 0, 82, 33, 15);
		moveHand("left", 147, 130, 140, 34, 34, 164);
		moveHand("right", 20, 40, 40, 30, 30, 80);
		sleep(2);
	}

	public void putitdown() {
		setHandSpeed("left", 0.90f, 0.90f, 0.90f, 0.90f, 0.90f, 0.90f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.75f, 0.85f, 0.95f, 0.85f);
		setHeadSpeed(0.75f, 0.75f);
		moveHead(20, 99);
		moveArm("left", 1, 45, 87, 31);
		moveArm("right", 0, 82, 33, 15);
		moveHand("left", 147, 130, 135, 34, 34, 35);
		moveHand("right", 20, 40, 40, 30, 30, 72);
		sleep(2);
	}

	public void dropit() {
		setHandSpeed("left", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.75f, 0.85f, 1.0f, 0.85f);
		setHeadSpeed(0.75f, 0.75f);
		moveHead(20, 99);
		moveArm("left", 1, 45, 87, 31);
		moveArm("right", 0, 82, 33, 15);
		sleep(3);
		moveHand("left", 60, 61, 67, 34, 34, 35);
		moveHand("right", 20, 40, 40, 30, 30, 72);
	}

	public void removeleftarm() {
		setHandSpeed("left", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.95f, 0.65f, 0.75f, 0.75f);
		setHeadSpeed(0.75f, 0.75f);
		moveHead(20, 100);
		moveArm("left", 71, 94, 41, 31);
		moveArm("right", 0, 82, 28, 15);
		moveHand("left", 60, 43, 45, 34, 34, 35);
		moveHand("right", 20, 40, 40, 30, 30, 72);
	}

	public void further() {
		setHandSpeed("left", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setHandSpeed("right", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.95f, 0.65f, 0.75f, 0.75f);
		setHeadSpeed(0.75f, 0.75f);
		moveHead(79, 100);
		moveArm("left", 0, 94, 28, 15);
		moveArm("right", 0, 82, 28, 15);
		moveHand("left", 42, 58, 87, 55, 71, 35);
		moveHand("right", 81, 50, 82, 60, 105, 113);
	}

	public void openlefthand() {
		moveHand("left", 0, 0, 0, 0, 0, 0);
	}

	public void openrighthand() {
		moveHand("right", 0, 0, 0, 0, 0, 0);
	}

	public void surrender() {
		setHandSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.75f, 0.85f, 0.95f, 0.85f);
		setArmSpeed("left", 0.75f, 0.85f, 0.95f, 0.85f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(90, 90);
		moveArm("left", 90, 139, 15, 80);
		moveArm("right", 90, 145, 37, 80);
		moveHand("left", 50, 28, 30, 10, 10, 76);
		moveHand("right", 10, 10, 10, 10, 10, 139);
	}

	public void pictureleftside() {
		setHandSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 0.75f, 0.85f, 0.95f, 0.85f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(109, 90);
		moveArm("left", 90, 105, 24, 80);
		moveArm("right", 0, 82, 28, 15);
		moveHand("left", 50, 86, 97, 74, 106, 119);
		moveHand("right", 81, 65, 82, 60, 105, 113);
	}

	public void picturerightside() {
		setHandSpeed("left", 0.85f, 0.85f, 0.85f, 0.85f, 0.85f, 0.85f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 0.85f, 0.85f, 0.85f, 0.85f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(109, 90);
		moveArm("left", 0, 94, 28, 15);
		moveArm("right", 90, 115, 23, 68);
		moveHand("left", 42, 58, 87, 55, 71, 35);
		moveHand("right", 10, 112, 95, 91, 125, 45);
	}

	public void picturebothside() {
		setHandSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setHandSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("right", 1.0f, 1.0f, 1.0f, 1.0f);
		setArmSpeed("left", 1.0f, 1.0f, 1.0f, 1.0f);
		setHeadSpeed(0.65f, 0.65f);
		moveHead(109, 90);
		moveArm("left", 90, 105, 24, 80);
		moveArm("right", 90, 115, 23, 68);
		moveHand("left", 50, 86, 97, 74, 106, 119);
		moveHand("right", 10, 112, 95, 91, 125, 45);
	}

	public void powerdown() {
		sleep(2);
		ear.pauseListening();
		rest();
		speakBlocking("I'm powering down");
		sleep(2);
		moveHead(40, 85);
		;
		sleep(4);
		// rightA
		// rightSerialPort.digitalWrite(53, Arduino.LOW);
		// leftSerialPort.digitalWrite(53, Arduino.LOW);
		ear.lockOutAllGrammarExcept("power up");
		sleep(2);
		ear.resumeListening();
	}

	public void powerup() {
		sleep(2);
		ear.pauseListening();
		// rightSerialPort.digitalWrite(53, Arduino.HIGH);
		// leftSerialPort.digitalWrite(53, Arduino.HIGH);
		speakBlocking("Im powered up");
		rest();
		ear.clearLock();
		sleep(2);
		ear.resumeListening();
	}

	// ---------- canned gestures end ---------

	public void cameraOn() {
		if (opencv != null) {
			opencv.capture();
		}
	}

	public void cameraOff() {
		if (opencv != null) {
			opencv.stopCapture();
		}
	}

	// ---------- movement commands begin ---------

	public void handOpen(String which) {
		moveHand(which, 0, 0, 0, 0, 0);
	}

	public void handClose(String which) {
		moveHand(which, 130, 180, 180, 180, 180);
	}

	public void handRest(String which) {
		moveHand(which, 60, 40, 30, 40, 40);
	}

	public void openPinch(String which) {
		moveHand(which, 0, 0, 180, 180, 180);
	}

	public void closePinch(String which) {
		moveHand(which, 130, 140, 180, 180, 180);
	}

	public void setHandSpeed(String which, Float thumb, Float index, Float majeure, Float ringFinger, Float pinky) {
		setHandSpeed(which, thumb, index, majeure, ringFinger, pinky, null);
	}

	public void setHandSpeed(String which, Float thumb, Float index, Float majeure, Float ringFinger, Float pinky, Float wrist) {
		if (!hands.containsKey(which)) {
			error("setHandSpeed %s does not exist", which);
		} else {
			hands.get(which).setSpeed(thumb, index, majeure, ringFinger, pinky, wrist);
		}
	}

	public void moveHand(String which, Integer thumb, Integer index, Integer majeure, Integer ringFinger, Integer pinky) {
		moveHand(which, thumb, index, majeure, ringFinger, pinky, null);
	}

	public void moveHand(String which, Integer thumb, Integer index, Integer majeure, Integer ringFinger, Integer pinky, Integer wrist) {
		if (!hands.containsKey(which)) {
			error("moveHand %s does not exist", which);
		} else {
			hands.get(which).moveTo(thumb, index, majeure, ringFinger, pinky, wrist);
		}
	}

	public void setArmSpeed(String which, Float bicep, Float rotate, Float shoulder, Float omoplate) {
		if (!arms.containsKey(which)) {
			error("setArmSpeed %s does not exist", which);
		} else {
			arms.get(which).setSpeed(bicep, rotate, shoulder, omoplate);
		}
	}

	public void moveArm(String which, Integer bicep, Integer rotate, Integer shoulder, Integer omoplate) {
		if (!arms.containsKey(which)) {
			error("setArmSpeed %s does not exist", which);
		} else {
			arms.get(which).moveTo(bicep, rotate, shoulder, omoplate);
		}
	}

	public void moveHead(Integer neck, Integer rothead) {
		if (head != null) {
			head.moveTo(neck, rothead);
		} else {
			log.error("I have a null head");
		}
	}

	public void moveHead(Integer neck, Integer rothead, Integer eyeX, Integer eyeY, Integer jaw) {
		if (head != null) {
			head.moveTo(neck, rothead, eyeX, eyeY, jaw);
		} else {
			log.error("I have a null head");
		}
	}

	public void setHeadSpeed(Float rothead, Float neck) {
		head.setSpeed(rothead, neck, null, null, null);
	}

	public void moveEyes(Integer eyeX, Integer eyeY) {
		if (head != null) {
			head.moveTo(null, null, eyeX, eyeY, null);
		} else {
			log.error("I have a null head");
		}
	}

	public void shutdown() {
		speakBlocking("shutting down now");
		detach();
	}

	public String captureGesture() {
		return captureGesture(null);
	}

	public String captureGesture(String gestureName) {
		StringBuffer script = new StringBuffer();

		String indentSpace = "";

		if (gestureName != null) {
			indentSpace = "  ";
			script.append(String.format("def %s():\n", gestureName));
		}

		if (head != null) {
			script.append(indentSpace);
			script.append(head.getScript(getName()));
		}

		if (leftArm != null) {
			script.append(indentSpace);
			script.append(leftArm.getScript(getName()));
		}
		if (rightArm != null) {
			script.append(indentSpace);
			script.append(rightArm.getScript(getName()));
		}

		if (leftHand != null) {
			script.append(indentSpace);
			script.append(leftHand.getScript(getName()));
		}
		if (rightHand != null) {
			script.append(indentSpace);
			script.append(rightHand.getScript(getName()));
		}

		send("python", "appendScript", script.toString());

		return script.toString();
	}

	/*
	 * ------------------------- GETTERS SETTERS END
	 * -----------------------------
	 */

	public static void main(String[] args) {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.INFO);

		String leftPort = "COM15";
		
		String x = "";
		String b = x + null;
		if (b != null)
		log.info(b);
		
		
		Runtime.createAndStart("gui", "GUIService");
		Runtime.createAndStart("python", "Python");

		InMoov i01 = (InMoov) Runtime.createAndStart("i01", "InMoov");
		Tracking neck = i01.startHeadTracking(leftPort);
		i01.detach();
		//neck.startLKTracking();
		
		/*
		InMoovArm leftArm = i01.startLeftArm(leftPort);

		InMoovHead head = i01.startHead(leftPort);
		Tracking neck = i01.startHeadTracking(leftPort);

		neck.faceDetect();
		*/

		/*
		 * 
		 * log.warn(Runtime.buildDNA("head", "InMoovHead").toString());
		 * log.warn("----------------------------------------------");
		 * log.warn(Runtime.buildDNA("i01", "InMoov").toString());
		 * 
		 * Runtime.createAndStart("gui", "GUIService");
		 * 
		 * InMoov i01 = (InMoov) Runtime.createAndStart("i01", "InMoov");
		 * i01.startAll("COM15", "COM12"); i01.shutdown();
		 * 
		 * boolean leaveNow = true; if (leaveNow){ return; }
		 * 
		 * Runtime.createAndStart("gui", "GUIService");
		 * Runtime.createAndStart("python", "Python");
		 * 
		 * log.warn(Runtime.buildDNA("i01", "InMoov").toString());
		 * 
		 * 
		 * // log.info(Runtime.buildDNA("i01.head", "InMoovHead").toString());
		 * 
		 * 
		 * 
		 * Tracking eyes = i01.getEyesTracking(); Tracking neck =
		 * i01.getHeadTracking();
		 * 
		 * eyes.faceDetect();
		 * 
		 * i01.startRightHand("COM4");
		 * 
		 * Runtime.createAndStart("gui", "GUIService");
		 * 
		 * i01.addRoutes(); i01.startMouth();
		 * 
		 * //i01.getRightArm().getShoulder().setRest(39);
		 * 
		 * //head.rothead.moveTo(96); //head.rothead.moveTo(150);
		 * //head.rothead.moveTo(88);
		 * 
		 * Tracking t = i01.getHeadTracking(); t.x.moveTo(30); t.x.moveTo(90);
		 * t.x.moveTo(150); t.x.moveTo(90); t.x.moveTo(30); t.x.setSpeed(0.2f);
		 * t.x.moveTo(30); t.x.moveTo(90); t.x.moveTo(150); t.x.moveTo(90);
		 * t.x.moveTo(30);
		 * 
		 * // i01.getHeadTracking().faceDetect();
		 * 
		 * // get("eyesTracking");
		 * 
		 * 
		 * 
		 * InMoovHand hand = i01.startRightHand("COM12"); hand.close();
		 * hand.moveTo(30, 30, 30, 30, 30); hand.moveTo(40, 40, 40, 40, 40);
		 * hand.moveTo(60, 60, 60, 60, 60, 60); hand.detach(); hand.moveTo(30,
		 * 30, 30, 30, 30);
		 * 
		 * hand.attach(); hand.moveTo(30, 30, 30, 30, 30); hand.open();
		 * log.info("here"); Runtime.createAndStart("gui", "GUIService");
		 * 
		 * 
		 * // I2C - needs bus and address (if on BBB or RasPi) .. or Arduino -
		 * needs // port / bus & address // startRightHand(String port) //
		 * startRightHand(String bus, String address) // startRightHand(String
		 * port, String bus, String address)
		 */
	}

}
