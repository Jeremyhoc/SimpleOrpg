package com.openorpg.simpleorpg.common;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

public class NewTiledMap extends TiledMap {
	
	int mapObjects[][] = new int[this.getWidth()][this.getHeight()];

	public int[][] getMapObjects() {
		return mapObjects;
	}

	public NewTiledMap(String ref, boolean loadTileSets) throws SlickException {
		super(ref, loadTileSets);
		
		for (int x=0; x<this.getWidth(); x++) {
			for (int y=0; y<this.getHeight(); y++) {
				mapObjects[x][y] = -1;
			}
		}
		
		int groupID = 0;
		for (int objectID = 0; objectID < this.getObjectCount(groupID); objectID++) {
			int objectX = this.getObjectX(groupID, objectID);
			int objectY = this.getObjectY(groupID, objectID);
			int objectWidth = this.getObjectWidth(groupID, objectID);
			int objectHeight = this.getObjectHeight(groupID, objectID);

			for (int w=0; w<objectWidth; w+= 16) {
				for (int h=0; h<objectHeight; h+= 16) {
					int objectTileX = (objectX+w)/this.getTileWidth();
					int objectTileY = (objectY+h)/this.getTileHeight();
					mapObjects[objectTileX][objectTileY] = objectID;
				}
			}
		}
	}
	
}
