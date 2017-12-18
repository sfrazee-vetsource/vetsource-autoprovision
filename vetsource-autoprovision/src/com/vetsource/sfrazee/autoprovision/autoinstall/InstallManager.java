package com.vetsource.sfrazee.autoprovision.autoinstall;

import java.util.ArrayList;

import com.vetsource.sfrazee.autoprovision.configmanager.AppData;

public class InstallManager {
	
	private ArrayList<ApplicationInstall> installers = new ArrayList<ApplicationInstall>();
	private String directory;

	public InstallManager(String directory, String configName) {
		this.directory = directory;
	}
	
	public void installApplications(AppData[] appConfigs) {
		removeStaleInstallers();
		
		for(int i = 0; i < appConfigs.length; i++) {
			if((boolean)appConfigs[i].getVariable("install") == true) {
			
				installers.add(new ApplicationInstall(	appConfigs[i].getExecName(),
														appConfigs[i].getName(),
														appConfigs[i].getArgs()
													));
			}
		}
		for(int i = 0; i < installers.size(); i++) {
			ApplicationInstall thisInstaller = installers.get(i);
			thisInstaller.startInstall();
			System.out.println("Installing " + thisInstaller.getName());
		}
	}
	
	private void removeStaleInstallers() {
		for(int i = 0; i < installers.size(); i++) {
			ApplicationInstall thisInstaller = installers.get(i);
			if(thisInstaller.finishedInstalling()) {
				installers.remove(thisInstaller);
			}
		}
	}
	
	public boolean appsInstalling() {
		return getAppsInstalling().length > 0;
	}
	
	public ApplicationInstall[] getAppsInstalling() {
		removeStaleInstallers();
		
		return installers.toArray(new ApplicationInstall[installers.size()]);
	}
	
}
