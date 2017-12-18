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
	
	public void writeAllConfigs(Map<String, Object>[] configs) {
		this.close();
		this.emptyFile();
		this.writeConfigs(configs);
		this.close();
	}
	
	public void writeConfig(Map<String, Object> config) {
		if(!open) {
			this.open();
		}
		String[] configStrings = mapToStrings(config);
		System.out.println(Arrays.toString(configStrings));
		
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
