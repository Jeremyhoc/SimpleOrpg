package com.openorpg.simpleorpg.client;


import java.util.Properties;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.SystemManager;
import com.artemis.World;
import com.openorpg.simpleorpg.client.components.DrawableText;
import com.openorpg.simpleorpg.client.components.Movement;
import com.openorpg.simpleorpg.client.components.Networking;
import com.openorpg.simpleorpg.client.components.Visibility;
import com.openorpg.simpleorpg.client.systems.InputSystem;
import com.openorpg.simpleorpg.client.systems.NetworkingSystem;
import com.openorpg.simpleorpg.client.systems.RenderSystem;
import com.openorpg.simpleorpg.client.systems.WarpSystem;


public class Client extends BasicGame {
	private static final Logger logger = Logger.getLogger(Client.class);
	private World world;
	private SystemManager systemManager;
	private EntitySystem warpSystem;
	private EntitySystem renderSystem;
	private EntitySystem networkingSystem;
	private EntitySystem inputSystem;
	
	public Client() {
		super("Simple Orpg");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("Initializing game engine");
		
		try {
			AppGameContainer container = new AppGameContainer(new Client());
			container.setDisplayMode(800, 600, false);
			//container.setDisplayMode(1024, 768, false);
			container.start();
		} catch (SlickException ex) {
			logger.fatal(ex);
		}
	}



	@Override
	public void init(GameContainer container) throws SlickException {
		container.setVSync(true);
		container.setAlwaysRender(true);
		world = new World();
		systemManager = world.getSystemManager();
		warpSystem = systemManager.setSystem(new WarpSystem());
		renderSystem = systemManager.setSystem(new RenderSystem(container));
		networkingSystem = systemManager.setSystem(new NetworkingSystem());
		inputSystem = systemManager.setSystem(new InputSystem(container));
		systemManager.initializeAll();
		
		Entity player = world.createEntity();
		player.setGroup("PLAYER");
		player.setTag("YOU");
		Properties props = PropertiesLoader.getInstance().load("client.properties");
		String ip = props.getProperty("ip", "127.0.0.1");
		int port = Integer.valueOf(props.getProperty("port", "1234"));
		player.addComponent(new Networking(ip, port));
		player.addComponent(new Movement(100));
		player.refresh();
		
		
		Entity input = world.createEntity();
		input.setTag("INPUT");
		input.addComponent(new DrawableText());
		input.addComponent(new Visibility(true));
		
	}
	
	
	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		renderSystem.process();
		
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		world.loopStart();
		world.setDelta(delta);
		networkingSystem.process();
		warpSystem.process();
		inputSystem.process();
	}

}
