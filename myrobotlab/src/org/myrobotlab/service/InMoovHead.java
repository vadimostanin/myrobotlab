package org.myrobotlab.service;

import org.myrobotlab.framework.Errors;
import org.myrobotlab.framework.Peers;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.Status;
import org.myrobotlab.logging.LoggerFactory;
import org.slf4j.Logger;

public class InMoovHead extends Service {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(InMoovHead.class);
	
	transient public Servo jaw;
	transient public Servo eyeX;
	transient public Servo eyeY;
	transient public Servo rothead;
	transient public Servo neck;
	transient public Arduino arduino;

	// static in Java are not overloaded but overwritten - there is no polymorphism for statics
	public static Peers getPeers(String name)
	{
		Peers peers = new Peers(name);
		
		peers.put("jaw", "Servo", "Jaw servo");
		peers.put("eyeX", "Servo", "Eyes pan servo");
		peers.put("eyeY", "Servo", "Eyes tilt servo");
		peers.put("rothead", "Servo", "Head pan servo");
		peers.put("neck", "Servo", "Head tilt servo");
		peers.put("arduino", "Arduino", "Arduino controller for this arm");
				
		return peers;
	}
	
	public InMoovHead(String n) {
		super(n);
		jaw = (Servo) createPeer("jaw");
		eyeX = (Servo) createPeer("eyeX");
		eyeY = (Servo) createPeer("eyeY");
		rothead = (Servo) createPeer("rothead");
		neck = (Servo) createPeer("neck");
		arduino = (Arduino) createPeer("arduino");

		// connection details
		neck.setPin(12);
		rothead.setPin(13);
		jaw.setPin(26); 
		eyeX.setPin(22);
		eyeY.setPin(24);
		
		neck.setController(arduino);
		rothead.setController(arduino);
		jaw.setController(arduino);
		eyeX.setController(arduino);
		eyeY.setController(arduino);
		
		neck.setMinMax(20, 160);
		rothead.setMinMax(30, 150);
		// reset by mouth control
		jaw.setMinMax(10, 25); 
		eyeX.setMinMax(60,100);
		eyeY.setMinMax(50,100);
		
		neck.setRest(90);
		rothead.setRest(90);
		jaw.setRest(10);
		eyeX.setRest(80);
		eyeY.setRest(90);

	}

	@Override
	public void startService() {
		super.startService();
		jaw.startService();
		eyeX.startService();
		eyeY.startService();
		rothead.startService();
		neck.startService();
		arduino.startService();
	}

	// FIXME - make interface for Arduino / Servos !!!
	public boolean connect(String port) {
		startService(); // NEEDED? I DONT THINK SO....

		if (arduino == null) {
			error("arduino is invalid");
			return false;
		}

		arduino.connect(port);

		if (!arduino.isConnected()) {
			error("arduino %s not connected", arduino.getName());
			return false;
		}

		attach();
		rest();
		broadcastState();
		return true;
	}

	/**
	 * attach all the servos - this must be re-entrant and accomplish the
	 * re-attachment when servos are detached
	 * 
	 * @return
	 */
	public boolean attach() {
		eyeX.attach();
		eyeY.attach();
		jaw.attach();
		rothead.attach();
		neck.attach();
		/*
		arduino.servoAttach(eyeX);
		sleep(InMoov.MSG_DELAY);
		arduino.servoAttach(eyeY);
		sleep(InMoov.MSG_DELAY);
		arduino.servoAttach(jaw);
		sleep(InMoov.MSG_DELAY);
		arduino.servoAttach(rothead);
		sleep(InMoov.MSG_DELAY);
		arduino.servoAttach(neck);
		sleep(InMoov.MSG_DELAY);
		*/
		
		return true;
	}

	public void moveTo(Integer neck, Integer rothead) {
		moveTo(neck, rothead, null, null, null);
	}

	public void moveTo(Integer neck, Integer rothead, Integer eyeX, Integer eyeY) {
		moveTo(neck, rothead, eyeX, eyeY, null);
	}

	public void moveTo(Integer neck, Integer rothead, Integer eyeX, Integer eyeY, Integer jaw) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.moveTo %d %d %d %d %d", neck, rothead, eyeX, eyeY, jaw));
		}
		this.rothead.moveTo(rothead);
		this.neck.moveTo(neck);
		if (eyeX != null)
			this.eyeX.moveTo(eyeX);
		if (eyeY != null)
			this.eyeY.moveTo(eyeY);
		if (jaw != null)
			this.jaw.moveTo(jaw);
	}

	public void rest() {
		// initial positions
		setSpeed(1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		rothead.rest();
		neck.rest();
		eyeX.rest();
		eyeY.rest();
		jaw.rest();
	}

	// FIXME - should be broadcastServoState
	public void broadcastState() {
		// notify the gui
		rothead.broadcastState();
		neck.broadcastState();
		eyeX.broadcastState();
		eyeY.broadcastState();
		jaw.broadcastState();
	}

	public void detach() {	
		rothead.detach();
		neck.detach();
		eyeX.detach();
		eyeY.detach();
		jaw.detach();
	}

	public void release() {
		detach();
		rothead.releaseService();
		neck.releaseService();
		eyeX.releaseService();
		eyeY.releaseService();
		jaw.releaseService();
	}

	public void setSpeed(Float headXSpeed, Float headYSpeed, Float eyeXSpeed, Float eyeYSpeed, Float jawSpeed) {
		if (log.isDebugEnabled()){
			log.debug(String.format("%s setSpeed %.2f %.2f %.2f %.2f %.2f", getName(), headXSpeed, headYSpeed, eyeXSpeed, eyeYSpeed, jawSpeed));
		}
		
		rothead.setSpeed(headXSpeed);
		neck.setSpeed(headYSpeed);
		eyeX.setSpeed(eyeXSpeed);
		eyeY.setSpeed(eyeYSpeed);
		jaw.setSpeed(jawSpeed);
			
	}

	public String getScript(String inMoovServiceName) {
		return String.format("%s.moveHead(%d,%d,%d,%d,%d)\n", inMoovServiceName, neck.getPosition(), rothead.getPosition(), eyeX.getPosition(), eyeY.getPosition(),
				jaw.getPosition());
	}

	public void setpins(int headXPin, int headYPin, int eyeXPin, int eyeYPin, int jawPin) {
		log.info(String.format("setPins %d %d %d %d %d %d", headXPin, headYPin, eyeXPin, eyeYPin, jawPin));
		rothead.setPin(headXPin);
		neck.setPin(headYPin);
		eyeX.setPin(eyeXPin);
		eyeY.setPin(eyeYPin);
		jaw.setPin(jawPin);
	}

	public boolean isValid() {
		rothead.moveTo(rothead.getRest() + 2);
		neck.moveTo(neck.getRest() + 2);
		eyeX.moveTo(eyeX.getRest() + 2);
		eyeY.moveTo(eyeY.getRest() + 2);
		jaw.moveTo(jaw.getRest() + 2);
		return true;
	}
	
	public void test() {
		Errors errors = new Errors();
		try {
			if (arduino == null) {
				errors.add("arduino is null");
			}
			
			if (!arduino.isConnected()){
				errors.add("arduino not connected");
			}

			rothead.moveTo(rothead.getPosition() + 2);
			neck.moveTo(neck.getPosition() + 2);
			eyeX.moveTo(eyeX.getPosition() + 2);
			eyeY.moveTo(eyeY.getPosition() + 2);
			jaw.moveTo(jaw.getPosition() + 2);
			
		} catch (Exception e) {
			error(e);
		}

		info("test completed");
	}

	public void setLimits(int headXMin, int headXMax, int headYMin, int headYMax, int eyeXMin, int eyeXMax, int eyeYMin, int eyeYMax, int jawMin, int jawMax) {
		rothead.setMinMax(headXMin, headXMax);
		neck.setMinMax(headYMin, headYMax);
		eyeX.setMinMax(eyeXMin, eyeXMax);
		eyeY.setMinMax(eyeYMin, eyeYMax);
		jaw.setMinMax(jawMin, jawMax);
	}

	// ----- initialization end --------
	// ----- movements begin -----------

	@Override
	public String getDescription() {
		return "InMoov Head Service";
	}


}
