package com.openorpg.simpleorpg.server.net;

import java.net.Socket;

import org.apache.log4j.Level;

import com.openorpg.simpleorpg.server.Player;

public class LeaveGameHandler extends MessageHandler {

	@Override
	public void handleMessage(Socket socket) {
		try {
			synchronized(this) {
				log(Level.DEBUG, MSG_TYPE.REC, socket, "LEAVE_GAME");
				Player yourPlayer = players.get(socket);
				String leaveMessage = "CHAT:BROADCAST,#FF0000," + yourPlayer.getName() + " has left the game!";
				sendAll(leaveMessage);
				MessageHandler leaveMapHandler = new LeaveMapHandler();
				leaveMapHandler.handleMessage(socket);
				// Remove you from the game
				players.remove(socket);	
			}

		} catch (Exception ex) {
			log(Level.ERROR, socket, ex.getMessage(), ex.getCause());
		}
	}

}
