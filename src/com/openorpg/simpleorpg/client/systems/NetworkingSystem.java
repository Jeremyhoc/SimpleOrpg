package com.openorpg.simpleorpg.client.systems;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import org.newdawn.slick.geom.Vector2f;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.openorpg.simpleorpg.client.components.ColorComponent;
import com.openorpg.simpleorpg.client.components.DrawableText;
import com.openorpg.simpleorpg.client.components.Location;
import com.openorpg.simpleorpg.client.components.Networking;
import com.openorpg.simpleorpg.client.components.ResourceRef;
import com.openorpg.simpleorpg.client.components.ChatBubble;
import com.openorpg.simpleorpg.client.components.Timer;
import com.openorpg.simpleorpg.client.components.Warp;

public class NetworkingSystem extends BaseEntityProcessingSystem {
	private ComponentMapper<Networking> networkingMapper;
	private ComponentMapper<ResourceRef> resourceRefMapper;
	private ComponentMapper<DrawableText> drawableTextMapper;
	private ComponentMapper<Location> locationMapper;

	@SuppressWarnings("unchecked")
	public NetworkingSystem() {
		super(Networking.class);
	}

	@Override
	protected void initialize() {
		networkingMapper = new ComponentMapper<Networking>(Networking.class, world);
		resourceRefMapper = new ComponentMapper<ResourceRef>(ResourceRef.class, world);
		drawableTextMapper = new ComponentMapper<DrawableText>(DrawableText.class, world);
		locationMapper = new ComponentMapper<Location>(Location.class, world);
	}

	@Override
	protected void process(Entity e) {
		final Networking networking = networkingMapper.get(e);		
		if (!networking.isConnected()) {
			try {
				if (!networking.isConnecting()) {
					networking.setConnecting(true);
					// Connect to the server
					new Thread() {
						public void run() {
							try {
								String ip = networking.getIp();
								int port = networking.getPort();
								logger.info("Connecting to " + ip + ":" + port);
								final Socket clientSocket = new Socket(ip, port);
								clientSocket.setTcpNoDelay(true);
								networking.setSocket(clientSocket);
								
								// Start listening for messages from the server
								BufferedReader in = null;
								PrintWriter out = null;
								try {
									in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
									out = new PrintWriter(clientSocket.getOutputStream(), true);
									networking.setOut(out);
									networking.setConnected(true);
									logger.info("Connected to server");
									String receivedLine;
									while ((receivedLine = in.readLine()) != null) {
										logger.info(receivedLine);
										networking.getReceivedMessages().put(receivedLine);
										//networking.getReceivedMessages().
										//networking.getReceivedMessages().
									}
									
								} catch (Exception ex) {
									logger.fatal(ex);
									networking.setConnected(false);
								}
							} catch (Exception ex) {
								logger.fatal(ex);
							}
							
							networking.setConnecting(false);
						}
					}.start();
				}
				
			} catch(Exception ex) {
				logger.fatal(ex);
			}
			
		// Handle messages
		} else {
			ArrayBlockingQueue<String> receivedQueue = networking.getReceivedMessages();
			ArrayBlockingQueue<String> sendQueue = networking.getSendMessages();
			readMessages(receivedQueue);
			sendMessages(sendQueue, networking.getOut());
		}
	}
	
	private void sendMessages(ArrayBlockingQueue<String> sendQueue, PrintWriter out) {
		for (int i=0; i<sendQueue.size(); i++) {
			String message = sendQueue.poll();
			out.println(message);
		}
	}
	
	private void readMessages(ArrayBlockingQueue<String> receivedQueue) {
		for (int i=0; i<receivedQueue.size(); i++) {
			String message = receivedQueue.poll();
			String id = message.toUpperCase();
			if (message.contains(":")) {
				id = message.split(":")[0].toUpperCase();
			}
			int idIndex = message.indexOf(":");
			String payload = "";
			if (idIndex+1 < message.length()) {
				payload = message.substring(idIndex+1);
			}
			// CHAT:type,color,message,[playerid]
			if (id.equals("CHAT")) {
				logger.info("[chat] " + payload);
				String payloadParts[] = payload.split(",");
				if (payloadParts.length >= 3) {
					Entity chatEntity = world.createEntity();
					chatEntity.setGroup("CHAT");
					// SAY or BROADCAST
					String chatType = payloadParts[0];
					String color = payloadParts[1];
					chatEntity.addComponent(new ColorComponent(color));
					String chatMsg = "";
					
					if (chatType.equals("BROADCAST")) {
						chatMsg = payload.substring(payload.indexOf(color) + color.length() + 1);
						chatEntity.addComponent(new Timer(15 * 1000));
					} else if (chatType.equals("SAY")) {
						String playerId = payloadParts[2];
						chatMsg = payload.substring(payload.indexOf(playerId) + playerId.length() + 1);
						String bubbleMsg = chatMsg.substring(chatMsg.indexOf(":") + 1).trim();
						Entity player = world.getTagManager().getEntity(playerId);
						player.addComponent(new ChatBubble(bubbleMsg, 15 * 1000));
					}
					chatEntity.addComponent(new DrawableText(chatMsg));
					chatEntity.refresh();
				} else {
					logger.warn("Invalid CHAT:playerid,type,color,message");
				}
			// WARP:mapref,x,y
			} else if (id.equals("WARP")) {
				logger.info("[warp] " + payload);
				
				String payloadParts[] = payload.split(",");
				if (payloadParts.length == 3) {
					Warp warp = new Warp();
					warp.setMapRef(payloadParts[0]);
					int x = Integer.valueOf(payloadParts[1]);
					int y = Integer.valueOf(payloadParts[2]);
					warp.setPosition(new Vector2f(x,y));
					world.getTagManager().getEntity("YOU").addComponent(warp);
					world.getTagManager().getEntity("YOU").refresh();
				} else {
					logger.warn(payloadParts.length + "Invalid WARP:mapref,x,y");
				}
				
			// SET_REF:playerid,ref
			} else if (id.equals("SET_REF")) {
				logger.info("[set_ref] " + payload);
				String payloadParts[] = payload.split(",");
				Entity entity;
				entity = world.getTagManager().getEntity(payloadParts[0]);
				
				if (resourceRefMapper.get(entity) != null) {
					resourceRefMapper.get(entity).setResourceName(payloadParts[1]);
				} else {
					entity.addComponent(new ResourceRef(payloadParts[1]));
				}
				

			// PLAYER_JOINED_MAP:playerid,playername,playerimage,x,y
			} else if (id.equals("PLAYER_JOINED_MAP")) {
				logger.info("[player_joined_map] " + payload);
				String payloadParts[] = payload.split(",");
				if (payloadParts.length == 5) {
					Entity newPlayer = world.createEntity();
					newPlayer.setGroup("PLAYER");
					newPlayer.setTag(payloadParts[0]);
					newPlayer.addComponent(new DrawableText(payloadParts[1]));
					newPlayer.addComponent(new ResourceRef(payloadParts[2]));
					newPlayer.addComponent(new Location(Integer.valueOf(payloadParts[3]), Integer.valueOf(payloadParts[4])));
					newPlayer.refresh();
				} else {
					logger.warn(payloadParts.length + "Invalid PLAYER_JOINED_MAP:playerid,playername,playerimage,x,y");
				}
			// SET_NAME:playerid,name
			} else if (id.equals("SET_NAME")) {
				logger.info("[set_name] " + payload);
				String payloadParts[] = payload.split(",");
				Entity entity;
				entity = world.getTagManager().getEntity(payloadParts[0]);
				
				if (drawableTextMapper.get(entity) != null) {
					drawableTextMapper.get(entity).setText(payloadParts[1]);
				} else {
					entity.addComponent(new DrawableText(payloadParts[1]));
				}
			// PLAYER_MOVED:playerid,x,y
			} else if (id.equals("PLAYER_MOVED")) {
				logger.info("[player_moved] " + payload);
				String payloadParts[] = payload.split(",");
				if (payloadParts.length == 3) {
					Entity playerEntity = world.getTagManager().getEntity(payloadParts[0]);
					Vector2f playerLocation = locationMapper.get(playerEntity).getPosition();
					playerLocation.set(new Vector2f(Integer.valueOf(payloadParts[1]), Integer.valueOf(payloadParts[2])));
				} else {
					logger.warn(payloadParts.length + "Invalid PLAYER_JOINED_MAP:playerid,x,y");
				}
			// PLAYER_LEFT_MAP:playerid
			} else if (id.equals("PLAYER_LEFT_MAP")) {
				logger.info("[player_left_map] " + payload);
				world.getTagManager().getEntity(payload).delete();
			} else {
				logger.warn("Not doing anything with the payload: " + payload);
			}
				
		}
	}
}
