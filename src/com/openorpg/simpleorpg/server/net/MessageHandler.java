package com.openorpg.simpleorpg.server.net;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.openorpg.simpleorpg.server.Map;
import com.openorpg.simpleorpg.server.Player;
import com.openorpg.simpleorpg.shared.ResourceFactory;


public abstract class MessageHandler {

	private static final Logger messageLogger = Logger.getLogger(MessageHandler.class);
	protected final Logger logger = Logger.getLogger(getClass());
	protected static final HashMap<String, Map> maps = new HashMap<String, Map>();
	protected static final HashMap<Socket, Player> players = new HashMap<Socket, Player>();
	protected enum MSG_TYPE {
		SND, REC
	}
	
	// Load in maps from the database
	public static void init() {
		for (String mapRef : ResourceFactory.getInstance().getResourceIds("tiledmap")) {
			maps.put(mapRef, new Map());
		}
		
	}
	
	public static MessageHandler create(String message) {		
		String id = message;
		if (message.contains(":")) {
			id = message.split(":")[0].toUpperCase();
		}
		int idIndex = message.indexOf(":");
		String payload = "";
		if (idIndex+1 < message.length()) {
			payload = message.substring(idIndex+1);
		}
		
		if (id.equals("MOVE")) {
			return new MoveHandler(payload);
		} else if (id.equals("CHAT")) {
			return new ChatHandler(payload);
		} else if (id.equals("WHO")) {
			return new WhoHandler();
		} else if (id.equals("SET_NAME")) {
			return new SetNameHandler(payload);
		}
		
//		if (id.equals("JOIN_GAME")) {
//			return new JoinGameHandler();
//		} else if (id.equals("LEAVE_GAME")) {
//			return new LeaveGameHandler();
//		} else if (id.equals("JOIN_MAP")) {
//			return new JoinMapHandler();
//		} else {
			messageLogger.warn(id + " does not exist");
			return null;
//		}
	}
	
	protected synchronized void sendAllMapBut(Socket socket, String message) {
		Player yourPlayer = players.get(socket);
		Map map = maps.get(yourPlayer.getMapRef());
		
		for (Socket otherSocket : map.getPlayers().keySet()) {
			if (players.get(otherSocket).getId() != yourPlayer.getId()) {
				sendTo(otherSocket, message);
			}
		}
	}
	
	protected synchronized void sendAll(String message) {
		for (Socket socket : players.keySet()) {
			sendTo(socket, message);
		}
	}
	
	protected void sendTo(Socket socket, String message) {
		if (socket != null && !socket.isClosed() && !message.isEmpty()) {
			try {
				PrintWriter playerOut = new PrintWriter(socket.getOutputStream(), true);
				playerOut.println(message);
				log(Level.DEBUG, MSG_TYPE.SND, socket, message);
			} catch (Exception ex) {
				log(Level.ERROR, socket, message, ex.getCause());
			}
		}
	}
	
	protected String getIp(Socket socket) {
		return socket.getInetAddress().getHostAddress();
	}
	
	protected synchronized void log(Level level, MSG_TYPE type, Socket socket, String msg) {
		String name = "";
				
		if (socket != null &&  !socket.isClosed()) {
			name = socket.getInetAddress().getHostAddress();
			Player yourPlayer = players.get(socket);
			if (yourPlayer != null) {
				name = yourPlayer.getName();
			}
		}
		
		if (!name.equals("")) {
			logger.log(level, type + " [" + name +"] " + msg);
		} else {
			logger.log(level, type + " " + msg);
		}
	}
	
	protected synchronized void log(Level level, Socket socket, String msg, Throwable throwable) {
		String name = "";
		
		if (socket != null &&  !socket.isClosed()) {
			name = socket.getInetAddress().getHostAddress();
			Player yourPlayer = players.get(socket);
			if (yourPlayer != null) {
				name = yourPlayer.getName();
			}
		}
		
		if (!name.equals("")) {
			logger.log(level, "[" + name +"] " + msg, throwable);
		} else {
			logger.log(level, msg, throwable);
		}
	}
	
	
	
	public abstract void handleMessage(Socket socket);

}
