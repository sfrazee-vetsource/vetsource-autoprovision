package com.vetsource.sfrazee.autoprovision.autoinstall;

import java.util.ArrayList;

import com.vetsource.sfrazee.autoprovision.configmanager.AppData;

public class InstallManager {

	private ArrayList<ApplicationInstall> installers = new ArrayList<ApplicationInstall>();
	private String directory;

	public InstallManager(String directory, String configName) {
		this.directory = directory;
	}

	// Install all the applications listed in an array of AppData objects
	public void installApplications(AppData[] appConfigs) {
		// Make sure that we don't have installers listed that aren't running
		removeStaleInstallers();

		for (int i = 0; i < appConfigs.length; i++) {
			// Some installers might be marked to not be installed, so don't install those
			// ones
			if ((boolean) appConfigs[i].getVariable("install") == true) {

				// Create a new object for handling this executable's installation
				installers.add(new ApplicationInstall(appConfigs[i].getExecName(), appConfigs[i].getName(),
						appConfigs[i].getArgs()));
			}
		}

		// Loop through the list of all installers
		for (int i = 0; i < installers.size(); i++) {

			// For each installer object in the list, start the installer executable
			// associated with it
			ApplicationInstall thisInstaller = installers.get(i);
			thisInstaller.startInstall();
			System.out.println("Installing " + thisInstaller.getName());
		}
	}

	
	//Get rid of all installers tat have either finished or failed to install in the first place
	private void removeStaleInstallers() {
		
		//Loop through the list of installer objects
		for (int i = 0; i < installers.size(); i++) {
			ApplicationInstall thisInstaller = installers.get(i);
			
			//If the installer says that it is finished, remove it from the list
			if (thisInstaller.finishedInstalling()) {
				installers.remove(thisInstaller);
			}
		}
	}
	
	//Simple check to see if any installs are running
	public boolean appsInstalling() {
		return getAppsInstalling().length > 0;
	}
	
	//returns a list of apps that are installing
	public ApplicationInstall[] getAppsInstalling() {
		//Remove finished installers
		removeStaleInstallers();

		//Return the list of installers in the list
		return installers.toArray(new ApplicationInstall[installers.size()]);
	}

}
