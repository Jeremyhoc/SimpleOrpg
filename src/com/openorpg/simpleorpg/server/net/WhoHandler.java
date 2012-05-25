package com.openorpg.simpleorpg.server.net;

import java.net.Socket;

import com.openorpg.simpleorpg.server.Player;

public class WhoHandler extends MessageHandler {
	
	public WhoHandler() {
	}

	@Override
	public void handleMessage(Socket socket) {
		synchronized(this) {			
			String whoMessage = "CHAT:BROADCAST,#FFCC11,";
			String playerNames = "";
			Player yourPlayer = players.get(socket);
			
			int numPlayers = 0;
			for (Player player : players.values()) {
				if (player.getId() != yourPlayer.getId()) {
					if (playerNames.equals("")) playerNames = player.getName();
					else playerNames += ", " +  player.getName();
					numPlayers++;
				}
			}
			
			if (numPlayers == 1) whoMessage += "There is one other player online: " + playerNames;
			else if (numPlayers == 0) whoMessage += "There are no other players online.";
			else whoMessage += "There are " + numPlayers + " other players online: " + playerNames;
			
			sendTo(socket, whoMessage);
		}
	}

}
