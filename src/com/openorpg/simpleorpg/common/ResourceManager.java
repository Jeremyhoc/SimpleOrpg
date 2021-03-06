package com.openorpg.simpleorpg.common;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.newdawn.slick.SlickException;

public class ResourceManager {
	private static final ResourceManager instance = new ResourceManager();
	private final Logger logger = Logger.getLogger(getClass());
	private HashMap<String, Resource> resources = new HashMap<String, Resource>();
	
	private ResourceManager() {
	}
	
	public static ResourceManager getInstance() {
		return instance;
	}
	
	public Resource getResource(String id, boolean headless) {
		id = id.toLowerCase();
		Resource resource = null;
		ResourceFactory resourceFactory = ResourceFactory.getInstance();
		
		try {
			resource = resources.get(id);
			//resource = resourceFactory.create(id);
			if (resource == null) {
				// Create the resource
				resource = resourceFactory.create(id, headless);
				resources.put(id, resource);
			}
			
		} catch(SlickException ex) {
			logger.error(ex);
		}
		return resource;
	}
	
	public Resource getResource(String id) {
		id = id.toLowerCase();
		Resource resource = null;
		ResourceFactory resourceFactory = ResourceFactory.getInstance();
		
		try {
			resource = resources.get(id);
			//resource = resourceFactory.create(id);
			if (resource == null) {
				// Create the resource
				resource = resourceFactory.create(id, false);
				resources.put(id, resource);
			}
			
		} catch(SlickException ex) {
			logger.error(ex);
		}
		return resource;
	}
	

	public void unloadResource(String id) {
		id = id.toLowerCase();
		resources.remove(id);
	}

}
