package com.openorpg.simpleorpg.server.net;

import java.net.Socket;

import org.apache.log4j.Level;

import com.openorpg.simpleorpg.server.Player;

public class SetNameHandler extends MessageHandler {
	
	private String payload;
	public SetNameHandler(String payload) {
		this.payload = payload;
	}

	@Override
	public void handleMessage(Socket socket) {
		synchronized(this) {
			log(Level.DEBUG, MSG_TYPE.REC, socket, "SET_NAME:"+payload);
			Player yourPlayer = players.get(socket);
			payload = payload.replace(",", "").trim();
			if (isAlpha(payload) && payload.length() > 0 && payload.length() <= 20) {
				yourPlayer.setName(payload);
				sendAllMapBut(socket, "SET_NAME:" + yourPlayer.getId() + "," + payload);
			}
		}
	}
	
	boolean isAlpha(String st) {
		for (char c : st.toCharArray()) {
			if (c <= 31 || c >= 127) {
				return false;
			}
		}
		
		return true;
	}

}
