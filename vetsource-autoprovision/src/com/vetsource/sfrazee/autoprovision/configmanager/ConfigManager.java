package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//This class does everything related to app configuration files.
//It can read items and the variables associated with them into an array/object
//It can also change the configuration values and regenerate the file to reflect the changes
public class ConfigManager {

	private String configName; // The name of the config file - without the leading config/ or ending .conf
	private String configPath; // The full path of the config file
	private ArrayList<ConfigData> configData; // The arraylist that holds all the data
	private boolean dataLoaded = false; // Stores whether we've loaded the config file at least once since the object
										// was created

	// Construct the object with the name of the config file
	public ConfigManager(String configName) {
		this.configName = configName;
		this.configPath = "config/" + configName + ".conf";

		// Make sure that the config file exists before trying to read from it.
		// If it doesn't exist yet, create a blank file (true)
		checkFileExists(true);

	}

	// The main method for loading the file and parsing data from it
	public void loadConfig() {

		// If we've loaded config before, let's empty it and just re-read it from the
		// config file
		if (dataLoaded) {
			configData.clear();
		} else {
			// If this is the first time we loaded data, mark that we have
			dataLoaded = true;
			configData = new ArrayList<ConfigData>();
		}

		// Creating the configReader object for actually reading data from the file
		ConfigReader configReader = new ConfigReader(this.configPath);

		// Make sure that the file exists before we try to load it
		// If it doesn't exist, we'll just create it to prevent any problems later
		configReader.checkFileExists(true);

		// Create a temporary array of maps to store the data read from the file
		Map<String, Object>[] inputConfig = configReader.readAllConfigs();

		// Loop over all the maps of data
		for (int i = 0; i < inputConfig.length; i++) {
			Map<String, Object> thisMap = inputConfig[i];

			String[] keys = thisMap.keySet().toArray(new String[thisMap.size()]);

			// Get the type of config we're reading. This determines which class we create
			// an instance of
			// E.G.
			//
			// Looking for this part
			// VVV
			// |app
			// variable=something
			// goestoeleven=11
			//
			//
			//
			String thisType = (String) thisMap.get("configType");

			// Switch statement to check config type
			switch (thisType) {
			case ("app"):
				AppData thisAppData = new AppData();

				for (int j = 0; j < keys.length; j++) {
					String thisKey = keys[j];
					if (!thisKey.equals("configType")) {
						thisAppData.setVariable(thisKey, thisMap.get(thisKey));
					}
				}

				configData.add(thisAppData);
				break;

			// If the identifier doesn't match anything, just create a new generic config
			// object
			default:

				ConfigData thisData = new ConfigData();

				// This is all the same as above
				for (int j = 0; j < keys.length; j++) {
					String thisKey = keys[j];
					if (!thisKey.equals("configType")) {
						thisData.setVariable(thisKey, thisMap.get(thisKey));
					}
				}

				configData.add(thisData);
				break;
			}
		}

	}

	// Write the current stored configuration to the config file
	public void writeToConfigFile() {
		if (!dataLoaded) {
			loadConfig();
		}

		// Create an instance of ConfigWriter to handle file IO
		// I need to change the path to not be hardcoded, but I'm still testing the
		// writer
		ConfigWriter configWriter = new ConfigWriter("config/apps.test.conf");

		// Need a temporary map to hold config values as we pass them to the writer
		Map<String, Object> configMap = new HashMap<String, Object>();

		// Open the IO stream to the file
		configWriter.open();
		for (int i = 0; i < configData.size(); i++) {
			ConfigData thisConfig = configData.get(i);
			Map<String, Object> thisMap = thisConfig.getVariables();

			// The ConfigWriter doesn't inherently know what type of dataconfig we have, so
			// we have to check and add it as a variable to the map first
			if (thisConfig.getClass().isInstance(new AppData())) {
				thisMap.put("configType", "app");
			} else {
				thisMap.put("configType", "config");
			}

			// Pass the map to the writer and have it written
			configWriter.writeConfig(thisMap);
		}

		// Close the IO stream to the file so we don't have problems later
		configWriter.close();
	}

	// Adding a data config just creates a data object with the specified name
	public void addDataConfig(String configName) {

		// Make sure we don't have duplicate data
		if (!dataConfigExists(configName)) {
			configData.add(new ConfigData(configName));
		}
	}

	// Delete a data config
	public void delDataConfig(String configName) {
		if (dataConfigExists(configName)) {
			for (int i = 0; i < configData.size(); i++) {

				// Find the config with the specified name, get it's index in the list, then
				// just pass that index to the delDataConfig method for ints
				if (configData.get(i).getName().equals(configName)) {
					delDataConfig(i);
				}
			}
		}
	}

	// Remove a data config object from the master list
	public void delDataConfig(int id) {
		configData.remove(id);
	}

	// If we already have the object associated with a data config entry, we can
	// delete it directly
	public void delDataConfig(ConfigData dataConfig) {
		configData.remove(dataConfig);
	}

	// Change some data in the config arraylist
	public void changeConfig(String configName, String variableName, String value) {
		int appId = -1;

		for (int i = 0; i < configData.size(); i++) {
			String thisName = configData.get(i).getName();
			if (thisName.equals(configName)) {
				appId = i;
			}
		}

		// If we actually found an app with the right name, just call the setVariable
		// method for that object with the values we passed in at the beginning
		if (appId != -1) {

			configData.get(appId).setVariable(variableName, value);
		}
	}

	// Check to see if the config file already exists, and if it doesn't, maybe
	// (if we passed true at the beginning) create it
	private boolean checkFileExists(boolean create) {
		File configFile = new File(this.configPath);
		if (configFile.exists()) {
			return true;
		} else {
			// If we sent true at the beginning, create the file if it doesn't already exist
			if (create) {
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
	}

	// Delete the config file
	private void deleteConfigFile() {
		File configFile = new File(this.configPath);
		if (configFile.exists()) {
			configFile.delete();
		}
	}

	// Return an array of data of the objects that we currently have in config
	public String[] getDataFromConfig() {
		if (!dataLoaded) {
			loadConfig();
		}

		String[] appList = new String[configData.size()];
		for (int i = 0; i < configData.size(); i++) {
			appList[i] = configData.get(i).getName();
		}

		return appList;
	}
	
	//Vanity printing method
	public void printConfig() {
		System.out.println("\n-----Config loaded from " + this.configName + ".conf -----");

		for (int i = 0; i < configData.size(); i++) {
			ConfigData thisConfig = this.getData(i);

			System.out.println("Config# " + i + ": " + thisConfig.getName());

			String[] keys = thisConfig.getVariables().keySet().toArray(new String[thisConfig.getVariables().size()]);

			for (int j = 0; j < keys.length; j++) {
				if (!keys[j].equals("name")) {
					System.out.println(keys[j] + ": " + thisConfig.getVariable(keys[j]));
				}
			}

			System.out.println();
		}
	}

	public boolean dataConfigExists(String configName) {
		for (int i = 0; i < configData.size(); i++) {
			if (configData.get(i).getName().equals(configName)) {
				return true;
			}
		}
		return false;
	}

	public ConfigData getData(int id) {
		return configData.get(id);
	}

	public ConfigData[] getDataConfigs() {
		return configData.toArray(new ConfigData[configData.size()]);
	}
}