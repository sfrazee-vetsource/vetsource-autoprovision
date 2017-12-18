package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.File;

public class AppData extends ConfigData {
	
	//Constructors are more detailed than the general ConfigData Class
	public AppData(String appName, String execName, String args) {
		this.setVariable("name", appName);
		this.setVariable("execName", execName);
		this.setVariable("args", args);
	}
	
	public AppData(String appName, String execName) {
		this.setVariable("name", appName);
		this.setVariable("execName", execName);
		this.setVariable("args", "");
	}
	
	public AppData(String execName) {
		this.setVariable("name", execName);
		this.setVariable("execName", execName);
		this.setVariable("args", "");
	}
	
	public AppData() {
		this.setVariable("name", null);
		this.setVariable("execName", null);
		this.setVariable("args", null);
	}

	
	//Getters and setters for AppData specific variables for ease of use
	public String getExecName() {
		return (String)this.getVariable("execName");
	}
	
	public void setExecName(String execName) {
		this.setVariable("execName", execName);
		
		if(this.getName() == null || this.getName() == "") {
			this.setName(execName);
		}
	}
	
	public String getArgs() {
		return (String)this.getVariable("args");
	}
	
	public void setArgs(String args) {
		this.setVariable("args", args);
	}
	
	public int getId() {
		return (int)this.getVariable("id");
	}
	
	public void setId(int id) {
		this.setVariable("id", id);
	}
	
	public boolean executableExists() {
		return new File("installers\\" + this.getExecName()).exists();
	}
}
