package com.vetsource.sfrazee.autoprovision.autoinstall;

import java.io.IOException;

public class ApplicationInstall {

	private String appName; 					//The name of the application that's going to be installed for reporting purposes
	private String appPath;						//The path of the installer executable
	private String appExtension;				//Filename extension for this executable
	private String quietArgs;					//The executable arguments we can use to install this application silently
	private String execName;
	private Process installerProcess;
	private boolean installStarted = false;
	private boolean installFailed = false;
	
	public ApplicationInstall(String execName, String appName, String quietArgs) { //The full range of options we need to successfully do an autoatic quiet install
		this.appName = appName;
		this.execName = execName;
		this.appPath = "\"installers/" + execName + "\"";
		this.quietArgs = quietArgs;
		this.appExtension = getExtension();
	}
	
	public ApplicationInstall(String execName) {
		this.appPath = "\"installers/" + execName + "\"";
		this.execName = execName;
		this.appName = execName;
	}
	
	public int startInstall() {
		String[] installCmd = getInstallCmd();
		
		try {
			installerProcess = Runtime.getRuntime().exec(installCmd);
			installStarted = true;
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("Oops! Looks like the " + this.appName + " installer broke.");
			installFailed = true;
		}
		return 0;
	}
	
	//We can't just put quietArgs into the install command because we'll just end up with an array of strings inside the second index in an array of strings.
	//This method just transposes the quiet arguments into the same index in the install array + 1 index space.
	private String[] getInstallCmd() {
		String[] installCmd;
		if(this.appExtension.equals(".exe")) {
			installCmd = new String[2];
			installCmd[0] = appPath;
			
			installCmd[1] = quietArgs;
		} else if(this.appExtension.equals(".msi")) {
			installCmd = new String[3];
			installCmd[0] = "msiexec";
			
			installCmd[1] = "/i" + windowsFriendly(appPath);
			
			installCmd[2] = quietArgs;
		} else {
			System.out.println("Nothing matched");
			installCmd = new String[0];
		}
		
		return installCmd;
	}
	
	public boolean isInstalling() {
		
		if(installStarted) {
			try {
				return installerProcess.isAlive();
			} catch (java.lang.NullPointerException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean finishedInstalling() {
		return (!isInstalling() && installStarted) || installFailed;
	}
	
	public void killInstaller() {
		if(isInstalling()) {
			installerProcess.destroyForcibly();
		}
	}
	
	public String getExtension() {
		String extension = "";
		for(int i = execName.length()-1; i >= 0; i--) {
			if(execName.charAt(i) == '.') {
				for(int j = i; j < execName.length(); j++) {
					extension += execName.charAt(j);
				}
				return extension;
			}
		}
		
		return extension;
	}
	
	//Getters and setters
	public String getName() {
		return appName;
	}
	
	public void setName(String appName) {
		this.appName = appName;
	}
	
	public String getPath() {
		return appPath;
	}
	
	public void setPath(String appPath) {
		this.appPath = appPath;
	}
	
	public String getQuietArgs() {
		return quietArgs;
	}
	
	public void setQuietArgs(String quietArgs) {
		this.quietArgs = quietArgs;
	}
	
	public String windowsFriendly(String originalPath) {
		String windowsPath = "";
		for(int i = 0; i < originalPath.length(); i++) {
			char thisChar = originalPath.charAt(i);
			if(thisChar == '/') {
				windowsPath += '\\';
			} else {
				windowsPath += thisChar;
			}
		}
		
		return windowsPath;
	}
}