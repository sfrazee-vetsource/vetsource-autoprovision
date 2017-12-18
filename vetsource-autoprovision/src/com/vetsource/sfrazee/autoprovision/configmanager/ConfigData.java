package com.vetsource.sfrazee.autoprovision.configmanager;

import java.util.HashMap;
import java.util.Map;

public class ConfigData {
	
	//General map for storing data
	protected Map<String, Object> variableStore = new HashMap<String, Object>();

	// I don't like the idea of having unnamed configs, but it appears to be
	// necessary elsewhere in the program
	public ConfigData() {
	}

	public ConfigData(String configName) {
		this.setVariable("name", configName);
	}

	// General getters and setters
	public String getName() {
		return (String) this.variableStore.get("name");
	}

	public void setName(String configName) {
		this.variableStore.put("name", configName);
	}

	public Map<String, Object> getVariables() {
		return this.variableStore;
	}
	
	//Passthrough methods for interacting with the stored hashmap
	public Object getVariable(String key) {
		return this.variableStore.get(key);
	}

	public void setVariable(String key, Object value) {
		this.variableStore.put(key, value);
	}

}