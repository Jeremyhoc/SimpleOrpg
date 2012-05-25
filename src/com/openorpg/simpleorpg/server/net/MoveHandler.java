package com.openorpg.simpleorpg.server.net;

import java.net.Socket;

import com.openorpg.simpleorpg.server.Player;
import com.openorpg.simpleorpg.shared.NewTiledMap;
import com.openorpg.simpleorpg.shared.ResourceManager;

public class MoveHandler extends MessageHandler {
	
	private String payload;
	public MoveHandler(String payload) {
		this.payload = payload;
	}
	
	private synchronized void warpPlayer(Socket socket, String mapRef, int x, int y) {
		Player player = players.get(socket);
		
		// Leave the current map
		MessageHandler leaveMapHandler = new LeaveMapHandler();
		leaveMapHandler.handleMessage(socket);
		
		player.setLocation(x, y);
		player.setMapRef(mapRef);
		
		// Join the new map
		MessageHandler joinMapHandler = new JoinMapHandler();
		joinMapHandler.handleMessage(socket);
	}

	@Override
	public void handleMessage(Socket socket) {
		synchronized(this) {
			Player yourPlayer = players.get(socket);
			int newX = yourPlayer.getX(), newY = yourPlayer.getY();
			if (payload.equals("UP")) {
				newY -= 1;
			} else if (payload.equals("DOWN")) {
				newY += 1;
			} else if (payload.equals("LEFT")) {
				newX -= 1;
			} else if (payload.equals("RIGHT")) {
				newX += 1;
			}
			
			// Check for collisions
			ResourceManager manager = ResourceManager.getInstance();
			NewTiledMap tiledMap = (NewTiledMap)manager.getResource(yourPlayer.getMapRef(), true).getObject();
			int groupID = 0;
			int mapObjects[][] = tiledMap.getMapObjects();
			
			// Bounds check
			if (newY >= tiledMap.getHeight() || newX >= tiledMap.getWidth() || newY < 0 || newX < 0) {
				// Warp Right
				String warpMap = "";
				if (newX >= tiledMap.getWidth()) {
					warpMap = tiledMap.getMapProperty("Right", "");
					newX = 0;
				}
				
				// Warp Left
				if (newX < 0) {
					warpMap = tiledMap.getMapProperty("Left", "");
					newX = tiledMap.getWidth()-1;
				}
				
				// Warp Up
				if (newY < 0) {
					warpMap = tiledMap.getMapProperty("Up", "");
					newY = tiledMap.getHeight()-1;
				}
				
				// Warp Down
				if (newY >= tiledMap.getHeight()) {
					warpMap = tiledMap.getMapProperty("Down", "");
					newY = 0;
				}
				
				if (!warpMap.equals("")) {
					warpPlayer(socket, warpMap, newX, newY);
				}
				
			} else if (mapObjects[newX][newY] != -1) {
				int objectID = mapObjects[newX][newY];
				String objectType = tiledMap.getObjectType(groupID, objectID).toUpperCase();
				
				if (objectType.equals("WARP")) {
					String warpMap = tiledMap.getObjectProperty(groupID, objectID, "Map", yourPlayer.getMapRef());
					int warpX = Integer.valueOf(tiledMap.getObjectProperty(groupID, objectID, "X", "0"));
					int warpY = Integer.valueOf(tiledMap.getObjectProperty(groupID, objectID, "Y", "0"));
					warpPlayer(socket, warpMap, warpX, warpY);

				}
				
				
			// Check for collision
			} else if (tiledMap.getTileId(newX, newY, 3) == 0) {
			
				// Check to make sure the player isn't trying to move too fast
				//Long curTime = new Date().getTime();
				//if (curTime - yourPlayer.getLastMovedTime() > 25) {
					//yourPlayer.setLastMovedTime(curTime);
					yourPlayer.setLocation(newX, newY);
					
					// Send you all other players on the map
					String playerMoved = "PLAYER_MOVED:" + yourPlayer.getId() + "," + 
														   yourPlayer.getX() + "," + 
														   yourPlayer.getY();
					
					sendAllMapBut(socket, playerMoved);
				//} else {
				//	logger.info(socket.getInetAddress().getHostAddress() + " is trying to move too fast!");
				//}
			}
		}
	}
}
