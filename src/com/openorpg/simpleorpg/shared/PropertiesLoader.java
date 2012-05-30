package com.openorpg.simpleorpg.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesLoader {
	private static PropertiesLoader instance = new PropertiesLoader();
	private HashMap<String, Properties> propertiesMap = new HashMap<String,Properties>();
	
	private PropertiesLoader() {
	}
	
	public static PropertiesLoader getInstance() {
		return instance;
	}
	 public Properties load(String propertiesFile) {
		 Properties properties = propertiesMap.get(propertiesFile);
		 
		 if (properties == null) {
			 properties = new Properties();
			 try {
				 InputStream in = new FileInputStream(propertiesFile);
				 properties.load(in);
				 propertiesMap.put(propertiesFile, properties);
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		 }
		 return properties;
 }

}
