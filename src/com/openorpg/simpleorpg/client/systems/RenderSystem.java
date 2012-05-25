package com.openorpg.simpleorpg.client.systems;
import java.awt.Font;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.tiled.TiledMap;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;
import com.openorpg.simpleorpg.client.components.ColorComponent;
import com.openorpg.simpleorpg.client.components.DrawableText;
import com.openorpg.simpleorpg.client.components.Fade;
import com.openorpg.simpleorpg.client.components.Location;
import com.openorpg.simpleorpg.client.components.Networking;
import com.openorpg.simpleorpg.client.components.ResourceRef;
import com.openorpg.simpleorpg.client.components.ChatBubble;
import com.openorpg.simpleorpg.client.components.Visibility;
import com.openorpg.simpleorpg.shared.ResourceManager;
@SuppressWarnings("deprecation")
public class RenderSystem extends BaseEntitySystem {
	private ComponentMapper<ResourceRef> resourceRefMapper;
	private ComponentMapper<Location> locationMapper;
	private GameContainer container;
	private ComponentMapper<ColorComponent> colorMapper;
	private ComponentMapper<Networking> networkingMapper;
	private ComponentMapper<DrawableText> drawableTextMapper;
	private ComponentMapper<ChatBubble> chatBubbleMapper;
	private ComponentMapper<Fade> fadeMapper;
	
	private TrueTypeFont nameFont;
	private TrueTypeFont inputFont;
	private TrueTypeFont saysFont;
	private TrueTypeFont chatFont;
	private ComponentMapper<Visibility> visibilityMapper;

	@SuppressWarnings("unchecked")
	public RenderSystem(GameContainer container) {
		super(ResourceRef.class, Location.class);
		this.container = container;
	}

	@Override
	protected void initialize() {
		resourceRefMapper = new ComponentMapper<ResourceRef>(ResourceRef.class, world);
		drawableTextMapper = new ComponentMapper<DrawableText>(DrawableText.class, world);
		locationMapper = new ComponentMapper<Location>(Location.class, world);
		colorMapper = new ComponentMapper<ColorComponent>(ColorComponent.class, world);
		networkingMapper = new ComponentMapper<Networking>(Networking.class, world);
		chatBubbleMapper = new ComponentMapper<ChatBubble>(ChatBubble.class, world);
		fadeMapper = new ComponentMapper<Fade>(Fade.class, world);
		visibilityMapper = new ComponentMapper<Visibility>(Visibility.class, world);
		saysFont = new TrueTypeFont(new java.awt.Font("Verdana", Font.BOLD, 12), false);
		nameFont = new TrueTypeFont(new java.awt.Font("Verdana", Font.PLAIN, 12), false);
		inputFont = new TrueTypeFont(new java.awt.Font("Verdana", Font.BOLD, 12), false);
		chatFont = new TrueTypeFont(new java.awt.Font("Verdana", Font.PLAIN, 14), false);
	}


	@Override
	protected boolean checkProcessing() {
		return true;
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		Graphics graphics = container.getGraphics();
		ImmutableBag<Entity> maps = world.getGroupManager().getEntities("MAP");
		ImmutableBag<Entity> players = world.getGroupManager().getEntities("PLAYER");
		ImmutableBag<Entity> chats = world.getGroupManager().getEntities("CHAT");
		ResourceManager manager = ResourceManager.getInstance();
		int tw = 32, th = 32;
		
		Entity yourPlayer = players.get(0);
		Networking networking = networkingMapper.get(yourPlayer);
		if (!networking.isConnected()) {
			String connectingString = "Connecting to " + networking.getIp() + ":" + networking.getPort() + "...";
			int cw = graphics.getFont().getWidth(connectingString);
			int ch = graphics.getFont().getHeight(connectingString);
			graphics.getFont().drawString(container.getWidth()/2 - cw/2, container.getHeight()/2 - ch/2, connectingString, Color.white);
		} else {
			// Render the ground & background
			for (int i=0; i<maps.size(); i++) {
				Entity mapEntity = maps.get(i);
				String ref = resourceRefMapper.get(mapEntity).getResourceName();
				TiledMap tiledMap = (TiledMap)manager.getResource(ref).getObject();
				tiledMap.render(0, 0, 0);
				tiledMap.render(0, 0, 1);
				tw = tiledMap.getTileWidth();
				th = tiledMap.getTileHeight();
			}
			
			// Render players
			for (int i=0; i<players.size(); i++) {
				Entity playerEntity = players.get(i);
				if (resourceRefMapper.get(playerEntity) != null) {
					String ref = resourceRefMapper.get(playerEntity).getResourceName();
					Image playerImage = (Image)manager.getResource(ref).getObject();
					Location playerLocation = locationMapper.get(playerEntity);
					if (playerLocation != null && playerImage != null) {
						Vector2f playerPosition = playerLocation.getPosition();
						graphics.drawImage(playerImage, playerPosition.x * tw, playerPosition.y * th);
					}
				}
			}
			
			// Render the foreground
			for (int i=0; i<maps.size(); i++) {
				Entity mapEntity = maps.get(i);
				String ref = resourceRefMapper.get(mapEntity).getResourceName();
				TiledMap tiledMap = (TiledMap)manager.getResource(ref).getObject();
				tiledMap.render(0, 0, 2);
			}
			

			
			// Render player names and bubble text
			for (int i=0; i<players.size(); i++) {
				Entity playerEntity = players.get(i);
				DrawableText drawableText = drawableTextMapper.get(playerEntity);
				Location playerLocation = locationMapper.get(playerEntity);
				if (playerLocation != null && drawableText != null) {
					String playerName = drawableTextMapper.get(playerEntity).getText();
					Vector2f playerPosition = playerLocation.getPosition();
					int h = nameFont.getLineHeight();
					int w = nameFont.getWidth(playerName);
					nameFont.drawString(playerPosition.x*tw - w/2 + tw/2, playerPosition.y*th - h, playerName, Color.white);
					
					if (chatBubbleMapper.get(playerEntity) != null) {
						ChatBubble chatBubble = chatBubbleMapper.get(playerEntity);
					    w = saysFont.getWidth(chatBubble.getText());
						h = saysFont.getLineHeight();
						
						// Word wrap the bubbles
						int bubbleWidth = 150;
						int curX = 0;
						int curY = 0;
						int startX = (int)playerPosition.x*tw - w/2 + tw/2;
						int startY = (int)playerPosition.y*th - (int)(h*2.5);
						String chatWords[] = chatBubble.getText().split(" ");
						
						ArrayList<String> lines = new ArrayList<String>();
						String line = "";
						
						// Split words into lines
						for (String word : chatWords) {
							word += " ";
							
							// If the individual word is too long, limit it
							if (saysFont.getWidth(word) >= bubbleWidth) { 
								String st = "";
								for (char c : word.toCharArray()) {
									if (saysFont.getWidth(st+c+" ") < bubbleWidth) {
										st += c;
									} else {
										word = st + " ";
										break;
									}
								}
							}
							
							curX += saysFont.getWidth(word);
							if (curX >= bubbleWidth) {
								curX = saysFont.getWidth(word);
								lines.add(line);
								line = word;
							} else {
								line += word;
							}
						}
						if (!line.equals("")) lines.add(line);
						
						
						// Render the background bubble
						graphics.setColor(new Color(0,0,0,100));
						if (w > bubbleWidth) {
							startX = (int)playerPosition.x*tw - bubbleWidth/2 + tw/2;
							graphics.fillRoundRect(startX - 5, startY - (h * (lines.size()-1)) - 3, bubbleWidth + 6, lines.size()*h + 6, 5);
						} else {
							graphics.fillRoundRect(startX - 5, startY - (h * (lines.size()-1)) - 3, w + 6, lines.size()*h + 6, 5);
						}
						
						// Render each individual line
						for (int x=lines.size()-1; x>=0; x--) {
							startX = (int)playerPosition.x*tw - saysFont.getWidth(lines.get(x))/2 + tw/2;
							graphics.setColor(new Color(255,255,255,255));
							saysFont.drawString(startX, startY + curY, lines.get(x));
							curY -= h;
						}
						
						// If time has elapsed
						if (chatBubble.isFinished()) {
							playerEntity.removeComponent(chatBubble);
							playerEntity.refresh();
						}
					}
				}
			}
			
			// Render the map mask
			for (int i=0; i<maps.size(); i++) {
				Entity mapEntity = maps.get(i);
				String ref = resourceRefMapper.get(mapEntity).getResourceName();
				TiledMap tiledMap = (TiledMap)manager.getResource(ref).getObject();
				if (!tiledMap.getMapProperty("Mask", "").equals("")) {
					String maskParts[] = tiledMap.getMapProperty("Mask", "").split(",");
					if (maskParts.length == 4) {
						int r = Integer.valueOf(maskParts[0]);
						int g = Integer.valueOf(maskParts[1]);
						int b = Integer.valueOf(maskParts[2]);
						int a = Integer.valueOf(maskParts[3]);
						graphics.setColor(new Color(r,g,b,a));
						graphics.fillRect(0, 0, container.getWidth(), container.getHeight());
					}
				}
			}
			
			// Render the fading effect
			for (int i=0; i<maps.size(); i++) {
				Entity mapEntity = maps.get(i);
				if (fadeMapper.get(mapEntity) != null) {
					Fade fade = fadeMapper.get(mapEntity);
					int alpha = fade.getAlpha();
					graphics.setColor(new Color(0,0,0,alpha));
					graphics.fillRect(0, 0, container.getWidth(), container.getHeight());
					fade.tick();
					if (alpha <= 0) {
						mapEntity.removeComponent(fadeMapper.get(mapEntity));
					}
				}
			}
			
			// Render input text
			Entity input = world.getTagManager().getEntity("INPUT");
			if (visibilityMapper.get(input).isVisible()) {
				int chatHistoryHeight = 200;
				int inputHeight = 20;
				
				graphics.setColor(new Color(0,0,0,150));
				graphics.fillRect(0, container.getHeight()-inputHeight, container.getWidth(), inputHeight);
				graphics.setColor(new Color(0,0,0,100));
				graphics.fillRect(0, container.getHeight()-(inputHeight + chatHistoryHeight), container.getWidth(), chatHistoryHeight);
				graphics.setColor(new Color(255,255,255));
				String message = drawableTextMapper.get(input).getText();
				inputFont.drawString(5,container.getHeight()-inputFont.getLineHeight()-3, message);
				
				// Render chat text
				int chatTextHeight = chatFont.getLineHeight();
				int startIndex = 0;
				// Scroll if chat size is larger than the chat window
				if (chats.size() * chatTextHeight > chatHistoryHeight) {
					int diff = chats.size() * chatTextHeight - chatHistoryHeight;
					startIndex = diff / chatTextHeight + 1;					
				}
				for (int i=startIndex; i<chats.size(); i++) {
					Entity chat = chats.get(i);
					message = drawableTextMapper.get(chat).getText();
					Color color = colorMapper.get(chat).getColor();
					color = new Color(color.r, color.g, color.b, .8f);
					chatFont.drawString(5, container.getHeight() - (inputHeight + chatHistoryHeight) + chatTextHeight * (i-startIndex), message, color);
				}
			}
		}
		
	}
}
