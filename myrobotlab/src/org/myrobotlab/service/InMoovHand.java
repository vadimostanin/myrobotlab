package org.myrobotlab.service;

import org.myrobotlab.framework.Peers;
import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.LoggingFactory;
import org.slf4j.Logger;

public class InMoovHand extends Service {

	private static final long serialVersionUID = 1L;
	public final static Logger log = LoggerFactory.getLogger(InMoovHand.class);
	
	/**
	 * peer services
	 */
	transient public Servo thumb;
	transient public Servo index;
	transient public Servo majeure;
	transient public Servo ringFinger;
	transient public Servo pinky;
	transient public Servo wrist;
	transient public Arduino arduino;
	
	
	// needed ?? reflectively they are good interfaces and 
	// do not need to define their interaction !!
	// transient public Keyboard keyboard;
	// transient public XMPP xmpp;
	//transient public Speech speech;// FIXME speakErrors()
	
	public Servo getThumb() {
		return thumb;
	}

	public void setThumb(Servo thumb) {
		this.thumb = thumb;
	}

	public Servo getIndex() {
		return index;
	}

	public void setIndex(Servo index) {
		this.index = index;
	}

	public Servo getMajeure() {
		return majeure;
	}

	public void setMajeure(Servo majeure) {
		this.majeure = majeure;
	}

	public Servo getRingFinger() {
		return ringFinger;
	}

	public void setRingFinger(Servo ringFinger) {
		this.ringFinger = ringFinger;
	}

	public Servo getPinky() {
		return pinky;
	}

	public void setPinky(Servo pinky) {
		this.pinky = pinky;
	}

	public Servo getWrist() {
		return wrist;
	}

	public void setWrist(Servo wrist) {
		this.wrist = wrist;
	}

	public Arduino getArduino() {
		return arduino;
	}

	public void setArduino(Arduino arduino) {
		this.arduino = arduino;
	}

	// static in Java are not overloaded but overwritten - there is no polymorphism for statics
	public static Peers getPeers(String name)
	{
		Peers peers = new Peers(name);
		peers.put("thumb", "Servo", "Thumb servo");
		peers.put("index", "Servo", "Index servo");
		peers.put("majeure", "Servo", "Majeure servo");
		peers.put("ringFinger", "Servo", "RingFinger servo");
		peers.put("pinky", "Servo", "Pinky servo");
		peers.put("wrist", "Servo", "Wrist servo");
		peers.put("arduino", "Arduino", "Arduino controller for this arm");
		//peers.put("keyboard", "Keyboard", "Keyboard control");
		//peers.put("xmpp", "XMPP", "XMPP control");
		return peers;
	}
	
	public InMoovHand(String n) {
		super(n);	
		thumb = (Servo) createPeer("thumb");
		index = (Servo) createPeer("index");
		majeure = (Servo) createPeer("majeure");
		ringFinger = (Servo) createPeer("ringFinger");
		pinky = (Servo) createPeer("pinky");
		wrist = (Servo) createPeer("wrist");
		arduino = (Arduino) createPeer("arduino");
		
		thumb.setPin(2);
		index.setPin(3);
		majeure.setPin(4);
		ringFinger.setPin(5);
		pinky.setPin(6);
		wrist.setPin(7);

		thumb.setRest(0);
		index.setRest(0);
		majeure.setRest(0);
		ringFinger.setRest(0);
		pinky.setRest(0);
		wrist.setRest(90);
	}
	
	// FIXME make 
	// .isValidToStart() !!! < check all user data !!!
	
	@Override
	public void startService() {
		super.startService();
		thumb.startService();
		index.startService();
		majeure.startService();
		ringFinger.startService();
		pinky.startService();
		wrist.startService();
		arduino.startService();
	}

	// FIXME FIXME - this method must be called
	// user data needed
	/**
	 * connect - user data needed
	 * @param port
	 * @return
	 */
	public boolean connect(String port)
	{
		startService();

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
	 * attach all the servos - this must be re-entrant
	 * and accomplish the re-attachment when servos are detached
	 * 
	 * @return
	 */
	public boolean attach() 
	{		
		arduino.servoAttach(thumb);
		arduino.servoAttach(index);
		arduino.servoAttach(majeure);
		arduino.servoAttach(ringFinger);
		arduino.servoAttach(pinky);
		arduino.servoAttach(wrist);
		
		return true;
	}

	
	@Override
	public String getDescription() {
		return "used as a general template";
	}
	
	// TODO - waving thread fun
	public void moveTo(Integer thumb, Integer index, Integer majeure, Integer ringFinger, Integer pinky) {
		moveTo(thumb, index, majeure, ringFinger, pinky, null);
	}

	public void moveTo(Integer thumb, Integer index, Integer majeure, Integer ringFinger, Integer pinky, Integer wrist) {
		if (log.isDebugEnabled()){
			log.debug(String.format("%s.moveTo %d %d %d %d %d %d", getName(), thumb, index, majeure, ringFinger, pinky, wrist));
		}
		this.thumb.moveTo(thumb);
		this.index.moveTo(index);
		this.majeure.moveTo(majeure);
		this.ringFinger.moveTo(ringFinger);
		this.pinky.moveTo(pinky);
		if (wrist != null)this.wrist.moveTo(wrist);
	}

	public void rest() {
		// initial positions
		setSpeed(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f);
		
		thumb.moveTo(0);
		index.moveTo(0);
		majeure.moveTo(0);
		ringFinger.moveTo(0);
		pinky.moveTo(0);
		wrist.moveTo(90);
	}
	
	public void broadcastState() {
		// notify the gui
		thumb.broadcastState();
		index.broadcastState();
		majeure.broadcastState();
		ringFinger.broadcastState();
		pinky.broadcastState();
		wrist.broadcastState();
	}
	
	public void detach() {
		thumb.detach();
		index.detach();
		majeure.detach();
		ringFinger.detach();
		pinky.detach();
		wrist.detach();
	}

	public void release() {
		detach();
		thumb.releaseService();
		index.releaseService();
		majeure.releaseService();
		ringFinger.releaseService();
		pinky.releaseService();
		wrist.releaseService();
	}

	public void setSpeed(Float thumb, Float index, Float majeure, Float ringFinger, Float pinky, Float wrist) {
		this.thumb.setSpeed(thumb);
		this.index.setSpeed(index);
		this.majeure.setSpeed(majeure);
		this.ringFinger.setSpeed(ringFinger);
		this.pinky.setSpeed(pinky);
		this.wrist.setSpeed(wrist);
	}
	
	public boolean isValid()
	{
		thumb.moveTo(2);
		index.moveTo(2);
		majeure.moveTo(2);
		ringFinger.moveTo(2);
		pinky.moveTo(2);
		wrist.moveTo(92);	
		return true;
	}
	
	public void victory()
	{
		moveTo(150,0,0,180,180,90);
	}
	
	public void devilHorns()
	{
		moveTo(150,0,180,180,0,90);
	}

	public void hangTen()
	{
		moveTo(0,180,180,180,0,90);
	}
	
	public void bird()
	{
		moveTo(150,180,0,180,180,90);
	}
	
	public void thumbsUp()
	{
		moveTo(0,180,180,180,180,90);
	}
	
	public void ok()
	{
		moveTo(150,180,0,0,0,90);
	}
	
	public void one()
	{
		moveTo(150,0,180,180,180,90);
	}
	
	public void two()
	{
		victory();
	}

	public void three()
	{
		moveTo(150,0,0,0,180,90);
	}

	public void four()
	{
		moveTo(150,0,0,0,0,90);
	}

	public void five()
	{
		open();
	}
	
	public void count(){
		one();
		sleep(1);
		two();
		sleep(1);
		three();
		sleep(1);
		four();
		sleep(1);
		five();
	}


	
	public String getScript() {
		return String.format("%s.moveTo(%d,%d,%d,%d,%d,%d)\n", Python.makeSafeName(getName()), thumb.getPosition(), index.getPosition(), majeure.getPosition(),
				ringFinger.getPosition(), pinky.getPosition(), wrist.getPosition());
	}
	
	public void setpins(int thumb, int index, int majeure, int ringFinger, int pinky, int wrist){
		log.info(String.format("setPins %d %d %d %d %d %d", thumb, index, majeure, ringFinger, pinky, wrist));
		this.thumb.setPin(thumb);
		this.index.setPin(index);
		this.majeure.setPin(majeure);
		this.ringFinger.setPin(ringFinger);
		this.pinky.setPin(pinky);
		this.wrist.setPin(wrist);
	}
	
	// ----- initialization end --------
	// ----- movements begin -----------

	public void close() {
		moveTo(130, 180, 180, 180, 180);
	}
	public void open() {
		moveTo(0, 0, 0, 0, 0);
	}
	
	public void openPinch() {
		moveTo(0, 0, 180, 180, 180);
	}

	public void closePinch() {
		moveTo(130, 140, 180, 180, 180);
	}
	
	public static void main(String[] args) {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.INFO);

		InMoovHand rightHand = new InMoovHand("r01");
		Runtime.createAndStart("gui", "GUIService");
		rightHand.connect("COM12");
		rightHand.startService();	
		Runtime.createAndStart("webgui", "WebGUI");
		//rightHand.connect("COM12"); TEST RECOVERY !!!
		
		rightHand.close();
		rightHand.open();
		rightHand.openPinch();
		rightHand.closePinch();
		rightHand.rest();
		
		/*
		 * GUIService gui = new GUIService("gui"); gui.startService();
		 * 
		 */
	}

	
}
