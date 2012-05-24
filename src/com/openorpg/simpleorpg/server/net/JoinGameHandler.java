package com.openorpg.simpleorpg.server.net;
import java.net.Socket;

import com.openorpg.simpleorpg.server.Player;

public class JoinGameHandler extends MessageHandler {
	
	public JoinGameHandler() {
	}

	@Override
	public void handleMessage(Socket socket) {
		// Load the player from the database
		String playerName = socket.getInetAddress().getHostAddress();
		Player yourPlayer = new Player(playerName, 
								   	   "knightImage", 
								   	   "testmap");
		if ((int)(Math.random()*2) == 0) yourPlayer.setRef("mageImage");
		yourPlayer.setLocation(5, 5);
		
		try {
			sendTo(socket, "SET_REF:YOU," + yourPlayer.getRef());
			sendTo(socket, "SET_NAME:YOU," + yourPlayer.getName());
			synchronized(this) {
				// Add the player to the game
				players.put(socket, yourPlayer);
				
				String joinGameMessage = "CHAT:BROADCAST,#00FF00," + yourPlayer.getName() + " has joined the game!";
				String welcomeMessage = "CHAT:BROADCAST,#00FFFF,Welcome to SimpleOrpg!";
				sendTo(socket,welcomeMessage);
				WhoHandler whoHandler = new WhoHandler();
				whoHandler.handleMessage(socket);
				sendAll(joinGameMessage);
				
			}
			MessageHandler joinMapHandler = new JoinMapHandler();
			joinMapHandler.handleMessage(socket);

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
	}

}
