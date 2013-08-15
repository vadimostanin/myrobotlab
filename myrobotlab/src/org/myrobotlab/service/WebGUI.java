package org.myrobotlab.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.myrobotlab.fileLib.Zip;
import org.myrobotlab.framework.Message;
import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.net.BareBonesBrowserLaunch;
import org.myrobotlab.security.BasicSecurity;
import org.myrobotlab.webgui.WSServer;
import org.myrobotlab.webgui.WebServer;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WebGUI extends Service {

	// import javax.xml.transform.Transformer;

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(WebGUI.class.getCanonicalName());

	public Integer httpPort = 7777;
	public Integer wsPort = 7778;

	transient WebServer ws;
	transient WSServer wss;

	boolean spawnBrowserOnStartUp = true;
	
	public HashMap<String,String> clients = new HashMap<String,String>();
	
	public WebGUI(String n) {
		super(n, WebGUI.class.getCanonicalName());
	}

	public Integer getPort() {
		return httpPort;
	}

	public boolean startWebServer(Integer port) {
		subscribe(Runtime.getInstance().getIntanceName(), "getRegistry");
		try {
			if (port.equals(httpPort) && ws != null)
			{
				warn("web server already running on port %d", port);
				return true;
			}
			
			this.httpPort = port;
			if (ws != null) {
				ws.stop();
			}

			ws = new WebServer(this, port);
			ws.start();

			return true;

		} catch (IOException e) {
			error(e.getMessage());
		}
		return false;
	}

	public boolean startWebSocketServer(Integer port) {
		try {
			
			if (port.equals(wsPort) && wss != null)
			{
				warn("web socket server already running on port %d", port);
				return true;
			}
			
			this.wsPort = port;

			if (wss != null) {
				wss.stop();
			}

			wss = new WSServer(this, port);
			wss.start();
			return true;
		} catch (Exception e) {
			Logging.logException(e);
		}

		return false;
	}

	public boolean start() {

		boolean result = true;
		result &= startWebServer(httpPort);
		result &= startWebSocketServer(wsPort);
		if (spawnBrowserOnStartUp)
		{
			//BareBonesBrowserLaunch.openURL(String.format("http://localhost:%d/services", httpPort));
			BareBonesBrowserLaunch.openURL(String.format("http://127.0.0.1:%d/resource/WebGUI/myrobotlab.html", httpPort));
		}
		return result;
	}

	@Override
	public String getToolTip() {
		return "The new web enabled GUI 2.0 !";
	}

	public void startService() {
		super.startService();
		start();
	}

	@Override
	public void stopService() {
		try {
			super.stopService();
			if (ws != null) {
				ws.stop();
			}
			if (wss != null) {
				wss.stop();
			}
		} catch (Exception e) {
			Logging.logException(e);
		}
	}

	public boolean preProcessHook(Message m) {
		// FIXME - problem with collisions of this service's methods
		// and dialog methods ?!?!?
		
		// if the method name is == to a method in the GUIService
		if (methodSet.contains(m.method)) {
			// process the message like a regular service
			return true;
		}
		
		// otherwise send the message to the dialog with the senders name
		sendToAll(m);
		return false;
	}
	
	public void customize()
	{
		try {
		(new File("root/resource/WebGUI")).mkdirs();
		//Zip.extractFromResource("/resource/WebGUI/Python.js", "root");
		Zip.extractFromFile("./libraries/jar/myrobotlab.jar", "root", "resource/WebGUI");
		} catch(Exception e){
			Logging.logException(e);
		}
	}

	// FIXME - take out of RESTProcessor - normalize
	public String toJson(Message msg) {
		try {
			//ByteArrayOutputStream out = null;
			//Gson gson = new Gson(); // FIXME - threadsafe? singleton?
			//Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create();
			
			/*
			 * Gson gson = new GsonBuilder()
     .registerTypeAdapter(Id.class, new IdTypeAdapter())
     .serializeNulls()
     .setDateFormat(DateFormat.LONG)
     .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
     .setPrettyPrinting()
     .setVersion(1.0)
     .create();
			 * 
			 */
			// http://google-gson.googlecode.com/svn/tags/1.2.3/docs/javadocs/com/google/gson/GsonBuilder.html#setDateFormat(int)
			// PRETTY PRINTING IS AWESOME ! MAKE CONFIGURABLE - PRETTY PRINT ONLY WORKS IN TEXTMODE .setPrettyPrinting()
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
			//   .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
			// gson.setDateFormat(DateFormat.FULL);
			/* REMOVED RECENTLY
			out = new ByteArrayOutputStream(); // FIXME - threadsafe? singleton?
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8")); // FIXME - threadsafe? singleton?
			gson.toJson(msg, Message.class, writer);
			*/
			//writer.setIndent("  "); // TODO config driven - very cool !

			//writer.beginArray();
			
			String ret = gson.toJson(msg, Message.class);
			//log.info(ret);
			// for (Message message : messages) {
			// gson.toJson(message, Message.class, writer);
			// }
			//writer.endArray();
			
			// writer.close();
			return ret;
		} catch (Exception e) {
			Logging.logException(e);
		}
		return null;
	}

	public int messages = 0;
	
	public void sendToAll(Message msg) {
		++messages;
		String json = toJson(msg);
		log.debug(String.format("webgui ---to---> all clients [%s]", json));
		if (messages%500 == 0) {
			info(String.format("sent %d messages to %d clients", messages, wss.connections().size())); // TODO modulus
		}
		
		if (json != null){
			wss.sendToAll(json);
		} else {
			log.error(String.format("toJson %s.%s is null", msg.name, msg.method));
		}
	}

	public static void main(String[] args) {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.INFO);

		// REST rest = new REST();
		// Runtime.createAndStart("arduino", "Arduino");
		//Clock clock = (Clock)Runtime.createAndStart("clock", "Clock");
		//clock.startClock();
		WebGUI webgui = (WebGUI)Runtime.createAndStart("webgui", "WebGUI");
		Runtime.createAndStart("python", "Python");
		webgui.addUser("gperry", "password");
		//Runtime.createAndStart("arduino", "Arduino");

//		webgui.subscribe("clock", "pulse");
		
//		webgui.subscribe("pulse", "clock", "pulse", String.class);
		
		/*
		Message msg = webgui.createMessage("webgui", "publishPin", new Object[] { new Pin(12, Pin.DIGITAL_VALUE, 1, "arduino") });
		webgui.sendToAll(msg);
		*/

		// FileIO.stringToFile("services.html", rest.getServices());

		// Runtime.releaseAll();
		// Runtime.createAndStart("gui", "GUIService");
		/*
		 * GUIService gui = new GUIService("gui"); gui.startService();
		 * gui.display();
		 */
	}
	
	public boolean addUser(String username, String password)
	{
		return BasicSecurity.addUser(username, password);
	}

}