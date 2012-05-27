package com.openorpg.simpleorpg.server.net;

import java.net.Socket;
import org.apache.log4j.Level;
import com.openorpg.simpleorpg.server.Player;

public class ChatHandler extends MessageHandler {
	
	private String payload;
	public ChatHandler(String payload) {
		this.payload = payload;
	}

	@Override
	public void handleMessage(Socket socket) {
		synchronized(this) {
			log(Level.DEBUG, MSG_TYPE.REC, socket, "CHAT:"+payload);
			Player yourPlayer = players.get(socket);
			if (payload.contains(",")) {
				String type = payload.split(",")[0].toUpperCase();
				if (payload.indexOf(",")+1 < payload.length()) {
					String message = payload.substring(payload.indexOf(",")+1);
					message = yourPlayer.getName() + ": " + message;
					
					if (message.length() > 150) { 
						message = message.substring(0, 150) + "..."; 
					}
					
					if (type.equals("BROADCAST")) {
						String broadcastMessage = "CHAT:BROADCAST," + 
														"#FF00FF," +
														message;
						//log(Level.INFO, true, socket, broadcastMessage);
						sendAll(broadcastMessage);
					} else if (type.equals("SAY")) {
						String sayMessage = "CHAT:" + "SAY," + 
													  "#FFFFFF," + 
													  yourPlayer.getId() + "," +
													  message;
						//log(Level.INFO, true, socket, sayMessage);
						sendAllMapBut(socket, sayMessage);
					}
				}
			}
		}
	}

}
