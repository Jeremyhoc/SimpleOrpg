package com.openorpg.simpleorpg.client.systems;
import java.util.concurrent.ArrayBlockingQueue;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;
import com.openorpg.simpleorpg.client.components.ColorComponent;
import com.openorpg.simpleorpg.client.components.DrawableText;
import com.openorpg.simpleorpg.client.components.Location;
import com.openorpg.simpleorpg.client.components.Movement;
import com.openorpg.simpleorpg.client.components.Networking;
import com.openorpg.simpleorpg.client.components.ResourceRef;
import com.openorpg.simpleorpg.client.components.ChatBubble;
import com.openorpg.simpleorpg.client.components.Timer;
import com.openorpg.simpleorpg.client.components.Visibility;
import com.openorpg.simpleorpg.shared.NewTiledMap;
import com.openorpg.simpleorpg.shared.ResourceManager;

public class InputSystem extends BaseEntitySystem implements KeyListener {
	private GameContainer container;
	private ComponentMapper<DrawableText> drawableTextMapper;
	private ComponentMapper<Timer> timerMapper;
	private ComponentMapper<Networking> networkingMapper;
	private ComponentMapper<Location> locationMapper;
	private Character c = null;
	private ComponentMapper<Movement> movementMapper;
	private ComponentMapper<ResourceRef> resourceRefMapper;
	private ComponentMapper<Visibility> visibilityMapper;
	
	private boolean key_back = false,
					key_enter = false,
					key_tab = false,
					key_up = false,
					key_down = false,
					key_left = false,
					key_right = false,
					key_esc = false;

	@SuppressWarnings("unchecked")
	public InputSystem(GameContainer container) {
		super(ResourceRef.class, Location.class);
		this.container = container;
	}

	@Override
	protected void initialize() {
		drawableTextMapper = new ComponentMapper<DrawableText>(DrawableText.class, world);
		timerMapper = new ComponentMapper<Timer>(Timer.class, world);
		networkingMapper = new ComponentMapper<Networking>(Networking.class, world);
		locationMapper = new ComponentMapper<Location>(Location.class, world);
		movementMapper = new ComponentMapper<Movement>(Movement.class, world);
		resourceRefMapper = new ComponentMapper<ResourceRef>(ResourceRef.class, world);
		visibilityMapper = new ComponentMapper<Visibility>(Visibility.class, world);
		container.getInput().addKeyListener(this);
	}


	@Override
	protected boolean checkProcessing() {
		return true;
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		Entity input = world.getTagManager().getEntity("INPUT");
		Entity yourEntity = world.getTagManager().getEntity("YOU");
		Location yourLocation = locationMapper.get(yourEntity);
		Movement yourMovement = movementMapper.get(yourEntity);
		Networking net = networkingMapper.get(yourEntity);
		ArrayBlockingQueue<String> sendMessages = net.getSendMessages();
		
		// Alphanumeric input interface (when you press enter chat shows up)
		if (key_enter) {
			DrawableText drawableText = drawableTextMapper.get(input);
			String sendText = drawableText.getText();
			
			if (!sendText.equals("") && visibilityMapper.get(input).isVisible()) {
				// Commands
				if (sendText.startsWith("/")) {
					String cmd = sendText.substring(1).toUpperCase().split(" ")[0];
					String cmdMsg = sendText.substring(sendText.indexOf(cmd) + cmd.length() + 2).replace(",", "").trim();
					
					// who
					if (cmd.equals("WHO")) {
						sendMessages.add("WHO");
					// setname <name>
					} else if (cmd.equals("SETNAME")) {
						String name = cmdMsg;
						
						if (name.length() > 0 && name.length() <= 20) {
							if (isAlpha(name)) {
								sendMessages.add("SET_NAME:" + name);
								drawableTextMapper.get(yourEntity).setText(name);
							}
						}
					// broadcast <msg>
					} else if (cmd.equals("BROADCAST")) {
						String broadcast = cmdMsg;
						
						if (broadcast.length() > 0) {
							sendMessages.add("CHAT:BROADCAST," + broadcast);
						}
					}
				// Normal chat
				} else {
					Entity chatEntity = world.createEntity();
					chatEntity.setGroup("CHAT");
					chatEntity.addComponent(new DrawableText("You: " + sendText));
					yourEntity.addComponent(new ChatBubble(sendText, 15 * 1000));
					chatEntity.addComponent(new ColorComponent(Color.white));
					chatEntity.refresh();
					sendMessages.add("CHAT:SAY," + sendText);
				}
				drawableText.setText("");
			}
		}
		
		// Add to the input text
		if (c != null && visibilityMapper.get(input).isVisible()) {
			DrawableText drawableText = drawableTextMapper.get(input);
			if (drawableText.getText().length() < 150)
				drawableText.setText(drawableText.getText() + c);
		}
		
		// Backspace
		if (key_back && visibilityMapper.get(input).isVisible()) {
			Timer timer = timerMapper.get(input);
			
			if (timer != null) {
				if (timer.isFinished()) {
					String txt = drawableTextMapper.get(input).getText();
					if (timer.isFinished() && txt.length() > 0) {
						timer.reset();
						drawableTextMapper.get(input).setText(txt.substring(0, txt.length()-1));
					}
					input.removeComponent(timer);
				}
			} else {
				input.addComponent(new Timer(75));
			}
		}
		
		// Hide the input & chat history
		if (key_tab) {
			visibilityMapper.get(input).setVisible(!visibilityMapper.get(input).isVisible());
		}
		
		// You can't hold down a letter or enter for input
		key_enter = false;
		key_tab = false;
		c = null;
		
		if (key_esc) {
			container.exit();
		}
		
		// Your movement
		if (yourLocation != null) {
			int newX = (int)yourLocation.getPosition().x, newY = (int)yourLocation.getPosition().y;
			int oldX = (int)yourLocation.getPosition().x, oldY = (int)yourLocation.getPosition().y;
			String moveMessage = "";
			
			if (key_up) {
				moveMessage = "MOVE:UP";
				newY -= 1;
			} else if (key_down) {
				moveMessage = "MOVE:DOWN";
				newY += 1;
			} else if (key_left) {
				moveMessage = "MOVE:LEFT";
				newX -= 1;
			} else if (key_right) {
				moveMessage = "MOVE:RIGHT";
				newX += 1;
			}
			
			ImmutableBag<Entity> maps = world.getGroupManager().getEntities("MAP");
			ResourceManager manager = ResourceManager.getInstance();
			
			if (key_up || key_down || key_left || key_right) {
				// Check for collision & move the player
				if (maps.get(0) != null) {
					String mapResName = resourceRefMapper.get(maps.get(0)).getResourceName();
					NewTiledMap map = (NewTiledMap)manager.getResource(mapResName).getObject();
					// Bounds Check
					if (newX <= map.getWidth() && newY <= map.getHeight() &&
						newX >= -1 && newY >= -1) {
						// Collision Check
						int collisionId;
						try {
							collisionId = map.getTileId(newX, newY, 3);
						} catch (Exception ex) { collisionId = 0; }
						if (collisionId == 0) {
							yourMovement = movementMapper.get(yourEntity);

							if (yourMovement.isFinished()) {
								// Prevent desyncing issues by 'freezing' the player at the warp/edge of map
								if (oldX != -1 && oldY != -1 && oldX != map.getWidth() && oldY != map.getHeight()) {
									if (map.getMapObjects()[oldX][oldY] == -1) {
										sendMessages.add(moveMessage);
										yourLocation.getPosition().set(newX, newY);
										yourMovement.reset();
									}
								}
							}
						}
					}
				}
			}
		}

		
	}
	
	boolean isAlpha(String st) {
		for (char c : st.toCharArray()) {
			if (!isAlpha(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	boolean isAlpha(char c) {
		// If alphanumeric
		if (c > 31 && c < 127) {
			return true;
		}
		return false;
	}

	@Override
	public void keyPressed(int key, char c) {
		if (isAlpha(c)) this.c = c;
		switch (key) {
			case Input.KEY_LEFT:
				key_left = true;
				break;
			case Input.KEY_RIGHT:
				key_right = true;
				break;
			case Input.KEY_UP:
				key_up = true;
				break;
			case Input.KEY_DOWN:
				key_down = true;
				break;
			case Input.KEY_DELETE:
			case Input.KEY_BACK:
				key_back = true;
				break;
			case Input.KEY_ENTER:
				key_enter = true;
				break;
			case Input.KEY_TAB:
				key_tab = true;
				break;
			case Input.KEY_CAPITAL:
			case Input.KEY_INSERT:
			case Input.KEY_LSHIFT:
			case Input.KEY_RSHIFT:
			case Input.KEY_LCONTROL:
			case Input.KEY_RCONTROL:
			case Input.KEY_LALT:
			case Input.KEY_RALT:
				break;
			case Input.KEY_ESCAPE:
				key_esc = true;
				break;
		}
	}

	@Override
	public void keyReleased(int key, char c) {
		switch (key) {
			case Input.KEY_LEFT:
				key_left = false;
				break;
			case Input.KEY_RIGHT:
				key_right = false;
				break;
			case Input.KEY_UP:
				key_up = false;
				break;
			case Input.KEY_DOWN:
				key_down = false;
				break;
			case Input.KEY_DELETE:
			case Input.KEY_BACK:
				key_back = false;
				break;
			case Input.KEY_TAB:
				key_tab = false;
				break;
			case Input.KEY_ENTER:
				key_enter = false;
				break;
			case Input.KEY_ESCAPE:
				key_esc = false;
				break;
		}
		
	}
	@Override
	public void setInput(Input input) {		
	}
	
	@Override
	public boolean isAcceptingInput() {
		return true;
	}
	
	@Override
	public void inputEnded() {		
	}
	
	@Override
	public void inputStarted() {
		
	}
}
