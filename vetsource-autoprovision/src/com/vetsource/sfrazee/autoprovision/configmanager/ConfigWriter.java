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

	// Check that the config file being written to exists
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

	// Close the writer to prevent IO exceptions
	public void close() {
		if (open) {
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

	// Open the writing stream to edit the file
	public void open() {
		if (!open) {
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

	// Write all configs in a given array to the file
	public void writeAllConfigs(Map<String, Object>[] configs) {
		// Close the file so we don't cause IO errors
		this.close();

		// Empty the file so we don't get duplicate entries
		this.emptyFile();

		// Call the writeConfigs method to write the given configs to the file
		this.writeConfigs(configs);

		// Close the file
		this.close();
	}

	// Write a single config entry to the config file
	public void writeConfig(Map<String, Object> config) {

		// If the file isn't already open, open it to prevent IO exceptions
		if (!open) {
			this.open();
		}

		// Convert our map of variables into their string representations
		String[] configStrings = mapToStrings(config);

		// Loop through all the string variables
		for (int i = 0; i < configStrings.length; i++) {
			try {

				// Write the variable to file and move to the next line
				configBufferedWriter.write(configStrings[i]);
				configBufferedWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// Write multiple config entries to the file at once
	public void writeConfigs(Map<String, Object>[] configs) {

		// Loop through the array of config maps
		for (int i = 0; i < configs.length; i++) {

			// for each of the config entries, call the writeConfig method
			this.writeConfig(configs[i]);

			try {
				// Write an extra line after each entry (doesn't appear to be working for some
				// reason?)
				configBufferedWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Delete the associated config file (usually so we can make a new blank one)
	public void deleteConfigFile() {

		if (checkFileExists(false)) {
			configFile.delete();
		}
	}

	// Empty the config file (delete and create new empty file)
	public void emptyFile() {
		this.deleteConfigFile();
		this.checkFileExists(true);
	}

	// Convert a map of values into their file-writeable string representations
	private String[] mapToStrings(Map<String, Object> map) {

		// Get the entire list of variable names
		String[] keys = map.keySet().toArray(new String[map.size()]);

		// Create an empty array to hold the string results
		String[] variableStrings = new String[keys.length];

		// Loop through the variable names
		for (int i = 0; i < keys.length; i++) {
			String thisKey = keys[i];
			Object thisValue = map.get(thisKey);

			// In the special case that the variable is configType, use the special
			// identifier (pipe character) for the reader to find
			if (thisKey.equals("configType")) {

				// We need the pipe identifier to be the first thing written to the file, so if
				// we aren't already at the beginning of the array, I move the item that is at
				// the beginning of the array to the current position and just drop this
				// identifier string at the beginning
				if (i != 0) {
					// move first item to current index
					variableStrings[i] = variableStrings[0];
				}

				// Overwrite the first item in the array
				variableStrings[0] = "|" + thisValue;

			} else {

				// The defualt array .toString method only returns the array identifier, so
				// detect whether this object is an array and use the Arrays.toString method
				// if it is an array to print it properly
				if (thisValue.getClass().isArray()) {
					variableStrings[i] = thisKey + "=" + Arrays.toString((String[]) thisValue);

				} else {
					// If the name is anything else, we just drop it in as normal
					variableStrings[i] = thisKey + "=" + thisValue;
				}
			}
		}

		// Return the array of writeable strings
		return variableStrings;
	}
}
