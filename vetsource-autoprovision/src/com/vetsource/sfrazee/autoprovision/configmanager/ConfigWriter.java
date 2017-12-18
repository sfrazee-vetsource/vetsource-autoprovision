package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class ConfigWriter {
	
	private File configFile;
	private FileWriter configFileWriter;
	private BufferedWriter configBufferedWriter;
	private boolean open = false;
	
	public ConfigWriter(String configPath) {
		configFile = new File(configPath);
	}
	
	
	//Check that the config file being written to exists
	public boolean checkFileExists(boolean create) {
		if (configFile.exists()) {
			return true;
		} else {
			// If we sent that we want to create the file if it doesn't exist, create the
			// file
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
	
	//Close the writer to prevent IO exceptions
	public void close() {
		if(open) {
			try {
				configBufferedWriter.close();
				configFileWriter.close();
				
				configBufferedWriter = null;
				configFileWriter = null;
				
				this.open = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Open the writing stream to edit the file
	public void open() {
		if(!open) {
			try {
				configFileWriter = new FileWriter(configFile);
				configBufferedWriter = new BufferedWriter(configFileWriter);
				this.open = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			this.close();
			this.open();
		}
	}
	
	//Write all configs in a given array to the file
	public void writeAllConfigs(Map<String, Object>[] configs) {
		//Close the file so we don't cause IO errors
		this.close();
		
		//Empty the file so we don't get duplicate entries
		this.emptyFile();
		
		//Call the writeConfigs method to write the given configs to the file
		this.writeConfigs(configs);
		
		//Close the file
		this.close();
	}
	
	//Write a single config entry to the config file
	public void writeConfig(Map<String, Object> config) {
		
		//If the file isn't already open, open it to prevent IO exceptions
		if(!open) {
			this.open();
		}
		
		//Convert our map of variables into their string representations
		String[] configStrings = mapToStrings(config);
		
		//Loop through all the string variables
		for(int i = 0; i < configStrings.length; i++) {
			try {
				configBufferedWriter.write(configStrings[i]);
				configBufferedWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public void writeConfigs(Map<String, Object>[] configs) {
		for(int i = 0; i < configs.length; i++) {
			this.writeConfig(configs[i]);
			
			try {
				configBufferedWriter.newLine();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteConfigFile() {
		
		if(checkFileExists(false)) {
			configFile.delete();
		}
	}
	
	public void emptyFile() {
		this.deleteConfigFile();
		this.checkFileExists(true);
	}
	
	private String[] mapToStrings(Map<String, Object> map) {
		String[] keys = map.keySet().toArray(new String[map.size()]);
		String[] variableStrings = new String[keys.length];
		
		for(int i = 0; i < keys.length; i++) {
			String thisKey = keys[i];
			Object thisValue = map.get(thisKey);
			
			if(thisKey.equals("configType")) {
				if(i != 0) {
					variableStrings[i] = variableStrings[0];
				}
				variableStrings[0] = "|" + thisValue;
				
			} else {
			
				variableStrings[i] = thisKey + "=" + thisValue;
			}
		}
		
		return variableStrings;
	}
}
