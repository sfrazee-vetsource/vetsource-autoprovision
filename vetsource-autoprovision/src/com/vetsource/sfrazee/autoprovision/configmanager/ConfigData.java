package com.vetsource.sfrazee.autoprovision.configmanager;

import java.util.HashMap;
import java.util.Map;

public class ConfigData {

	protected Map<String, Object> variableStore = new HashMap<String, Object>();
	
	public ConfigData() {
		
	}
	
	public ConfigData(String configName) {
		this.setVariable("name", configName);
	}
	
	
	//General getters and setters
	
	public String getName() {
		return (String)this.variableStore.get("name");
	}
	
	public void setName(String configName) {
		System.out.println("Changed " + this.getName() + "\'s name to " + configName);
		this.variableStore.put("name", configName);
	}
	
	public Map<String, Object> getVariables() {
		return this.variableStore;
	}
	
	public Object getVariable(String key) {
		return this.variableStore.get(key);
	}
	
	public void setVariable(String key, Object value) {
		this.variableStore.put(key, value);
	}
	
	public String[] getVariablesStrings() {
		String[] variableStrings = new String[this.variableStore.size()];
		String[] keys = this.variableStore.keySet().toArray(new String[this.variableStore.size()]);
		
		for(int i = 0; i < variableStrings.length; i++) {
			variableStrings[i] = keys[i] + "=" + this.variableStore.get(keys[i]);
		}
		
		return variableStrings;
	}
	
}