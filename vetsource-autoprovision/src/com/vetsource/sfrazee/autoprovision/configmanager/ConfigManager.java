package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//This class does everything related to app configuration files.
//It can read items and the variables associated with them into an array/object
//It can also change the configuration values and regenerate the file to reflect the changes
public class ConfigManager {
	
	private String configName; //The name of the config file - without the leading config/ or ending .conf
	private String configPath; //The full path of the config file
	private ArrayList<ConfigData> configData; //The arraylist that holds all the data
	private boolean dataLoaded = false; //Stores whether we've loaded the config file at least once since the object was created
	
	//Construct the object with the name of the config file
	public ConfigManager(String configName) {
		this.configName = configName;
		this.configPath = "config/" + configName + ".conf";
		
		//Make sure that the config file exists before trying to read from it.
		//If it doesn't exist yet, create a blank file (true)
		checkFileExists(true);
		
	}
	
	//The main method for loading the file and parsing data from it
	public void loadConfig() {
		
		ConfigReader configReader = new ConfigReader(this.configPath);
		//If we've loaded config before, let's empty it and just re-read it from the config file
		if(dataLoaded) {
			configData.clear();
		} else {
			//If this is the first time we loaded data, mark that we have
			dataLoaded = true;
			configData = new ArrayList<ConfigData>();
		}
		
		configReader.checkFileExists(true);
		
		Map<String, Object>[] inputConfig = configReader.readAllConfigs();
		
		for(int i = 0; i < inputConfig.length; i++) {
			Map<String, Object> thisMap = inputConfig[i];
			
			String[] keys = thisMap.keySet().toArray(new String[thisMap.size()]);
			String thisType = (String)thisMap.get("configType");
			
			switch(thisType) {
			
			case("app"):
				AppData thisAppData = new AppData();
			
				for(int j = 0; j < keys.length; j++) {
					String thisKey = keys[j];
					if(!thisKey.equals("configType")) {
						thisAppData.setVariable(thisKey, thisMap.get(thisKey));
					}
				}
			
				configData.add(thisAppData);
				break;
			
			default:	
				
				ConfigData thisData = new ConfigData();
				
				for(int j = 0; j < keys.length; j++) {
					String thisKey = keys[j];
					if(!thisKey.equals("configType")) {
						thisData.setVariable(thisKey, thisMap.get(thisKey));
					}
				}
				
				configData.add(thisData);
				break;
			}
		}
		
		
	}
	
	//Write the current stored configuration to the config file
	public void writeToConfigFile() {
		if(!dataLoaded) {
			loadConfig();
		}
		
		ConfigWriter configWriter = new ConfigWriter("config/apps.test.conf");
		
		Map<String, Object> configMap = new HashMap<String, Object>();
		
		configWriter.open();
		for(int i = 0; i < configData.size(); i++) {
			ConfigData thisConfig = configData.get(i);
			Map<String, Object> thisMap = thisConfig.getVariables();
			
			if(thisConfig.getClass().isInstance(new AppData())) {
				thisMap.put("configType", "app");
			} else {
				thisMap.put("configType", "config");
			}
			
			configWriter.writeConfig(thisMap);
		}
		
		configWriter.close();
	}
	
	//Adding config for an app is really just adding a list inside the config list
	//It'll probably include creating a data object in the future, but for now this works
	public void addDataConfig(String configName) {
		
		if(!dataConfigExists(configName)) {
			configData.add(new ConfigData(configName));
		}
	}
	
	public void delDataConfig(String configName) {
		if(dataConfigExists(configName)) {
			for(int i = 0; i < configData.size(); i++) {
				if(configData.get(i).getName().equals(configName)) {
					delDataConfig(i);
				}
			}
		}
	}
	
	public void delDataConfig(int id) {
		configData.remove(id);
	}
	
	public void delDataConfig(ConfigData dataConfig) {
		configData.remove(dataConfig);
	}
	
	//Change some data in the config arraylist
	public void changeConfig(String configName, String variableName, String value) {
		int appId = -1;
		
		for(int i = 0; i < configData.size(); i++) {
			String thisName = configData.get(i).getName();
			if(thisName.equals(configName)) {
				appId = i;
			}
		}
		
		//This is all hardcoded, it'll get changed later
		if(appId != -1) {
			/*switch(variableName) {
			case "name":		configData.get(appId).setName(value);
									break;
			case "execName":	configData.get(appId).setExecName(value);
									break;
			case "args":		configData.get(appId).setArgs(value);
									break;
			}*/
			
			configData.get(appId).setVariable(variableName, value);
		}
	}
	
	//Check to see if the config file already exists, and if it doesn't, maybe create it
	private boolean checkFileExists(boolean create) {
		File configFile = new File(this.configPath);
		if (configFile.exists()) {
			return true;
		} else {
			//If we sent that we want to create the file if it doesn't exist, create the file
			if(create) {
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
	}
	
	//Delete the config file
	private void deleteConfigFile() {
		File configFile = new File(this.configPath);
		if(configFile.exists()) {
			configFile.delete();
		}
	}
	
	//Return an array of the apps that we currently have in config
	public String[] getDataFromConfig() {
		if(!dataLoaded) {
			loadConfig();
		}
		
		String[] appList = new String[configData.size()];
		for(int i = 0; i < configData.size(); i++) {
			appList[i] = configData.get(i).getName();
		}
		
		return appList;
	}
	
	public void printConfig() {
		System.out.println("\n-----Config loaded from " + this.configName + ".conf -----");

		for(int i = 0; i < configData.size(); i++) {			
			ConfigData thisConfig = this.getData(i);
			
			System.out.println("Config# " + i + ": " + thisConfig.getName());
			
			String[] keys = thisConfig.getVariables().keySet().toArray(new String[thisConfig.getVariables().size()]);
			
			for(int j = 0; j < keys.length; j++) {
				if(!keys[j].equals("name")) {
					System.out.println(keys[j] + ": " + thisConfig.getVariable(keys[j]));
				}
			}
			
			
			System.out.println();
		}
	}

	
	public boolean dataConfigExists(String configName) {
		for(int i = 0; i < configData.size(); i++) {
			if(configData.get(i).getName().equals(configName)) {
				return true;
			}
		}
		return false;
	}
	
/*	public boolean execConfigExists(String execName) {
		for(int i = 0; i < configData.size(); i++) {
						
			if(configData.get(i).getExecName().equals(execName)) {
				return true;
			}
		}
		return false;
	}
*/

/*
	public String getAppExec(String appName) {
		for(int i = 0; i < configData.size(); i++) {
			if(configData.get(i).getName().equals(appName)) {
				return configData.get(i).getExecName();
			}
		}
		return null;
	}
*/
	
	public ConfigData getData(int id) {
		return configData.get(id);
	}
	
	public ConfigData[] getDataConfigs() {
		return configData.toArray(new ConfigData[configData.size()]);
	}
}