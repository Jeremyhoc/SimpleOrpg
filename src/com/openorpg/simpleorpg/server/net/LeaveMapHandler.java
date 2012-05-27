package com.openorpg.simpleorpg.server.net;

import java.net.Socket;

import org.apache.log4j.Level;

import com.openorpg.simpleorpg.server.Map;
import com.openorpg.simpleorpg.server.Player;

public class LeaveMapHandler extends MessageHandler {

	@Override
	public void handleMessage(Socket socket) {
		try {			
			synchronized(this) {
				log(Level.DEBUG, MSG_TYPE.REC, socket, "LEAVE_MAP");
				Player yourPlayer = players.get(socket);
				
				//PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				Map map = maps.get(yourPlayer.getMapRef());
				// Remove you from the map
				map.getPlayers().remove(socket);
				
				String youLeftMap = "PLAYER_LEFT_MAP:" + yourPlayer.getId();
				String otherLeftMap = "";
				for (Socket otherSocket : map.getPlayers().keySet()) {
					Player player = map.getPlayers().get(otherSocket);
					// Send all other players on the map your player
					otherLeftMap += "PLAYER_LEFT_MAP:" + player.getId() + "\n";
					// Send you all other players on the map
					sendTo(otherSocket, youLeftMap);
				}
				sendTo(socket, otherLeftMap);
			}

		} catch (Exception ex) {
			log(Level.ERROR, socket, ex.getMessage(), ex.getCause());
		}
	}

}
