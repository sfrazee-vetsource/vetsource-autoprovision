package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigReader {

	private File configFile;
	private FileReader configFileReader;
	private BufferedReader configBufferedReader;
	private boolean open = false;

	public ConfigReader(String configPath) {
		this.configFile = new File(configPath);
	}

	// Method to read a single config entry from the file
	public HashMap<String, Object> readNextConfig() {
		// If we haven't opened the file yet, open it so we don't get IO errors
		if (!this.open) {
			this.open();
		}

		// Create a hashmap to store all the data that we're going to read
		HashMap<String, Object> returnMap = null;

		// Try/catch block for IO errors
		try {

			// Return to the last marked position
			configBufferedReader.reset();
			// Mark this location, so we hit the next block we can return to the line and
			// read it again
			configBufferedReader.mark(255);

			// Variable to store the current line and the type of config w're reading
			String line;
			String type = null;

			// Read lines until we hit the end of the file (or we break for the config)
			while ((line = configBufferedReader.readLine()) != null) {
				// Store the first character in the line
				char firstChar = line.charAt(0);

				// If we find the '|' character, break out of this loop (signifies we hit the
				// beginning of a config section)
				if (firstChar == '|') {
					// Store the characters after the pipe character
					type = line.substring(1);
					break;
				}

				// Mark this position in case we hit the pipe
				configBufferedReader.mark(255);
			}

			// If we found a config before hitting the end of the file, read all the
			// variables associated with it
			if (type != null) {
				// Create an instance of HashMap to signify that we found something
				returnMap = new HashMap<String, Object>();

				// Add the name of the config type that we found earlier
				returnMap.put("configType", type);

				// Read until we hit the end of the file (or we break for the next config)
				while ((line = configBufferedReader.readLine()) != null) {
					// Set the char to the null character instead of Java null to keep null pointer
					// errors from happening
					char firstChar = '\u0000';
					if (line.length() > 0) { // Make sure the line actually has letters in it before trying to set the
												// first character
						firstChar = line.charAt(0); // Store the first character to determine what it is that we;re
													// reading
					}

					// If we hit the pipe symbol, we're at the end of this config and the beginning
					// of the next one, so break out of the loop
					if (firstChar == '|') {
						// Reset to the marker so the next reading operation can read this line again
						configBufferedReader.reset();
						break;

						// Check to see if the first character is one that is acceptable for a variable
						// name (conveniently Jva has a built in identifier for both alphanumeric and
						// acceptable special characters)
					} else if (Character.isJavaIdentifierPart(firstChar)) {

						// Send the line to the variable parsing method
						Object[] thisVariable = parseVariable(line);

						// if the returned variable exists, put it in the array to be returned
						if (thisVariable != null) {
							returnMap.put((String) thisVariable[0], thisVariable[1]);
						}
					}

					// mark this line so we can return to it if needed
					configBufferedReader.mark(255);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnMap;
	}

	// Method to check if the file we're going to read already exists
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

	// Open the reading stream to get characters from the file
	public void open() {
		if (!this.open) {
			try {
				configFileReader = new FileReader(configFile);
				configBufferedReader = new BufferedReader(configFileReader);

				configBufferedReader.mark(255);
				this.open = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.close();
			this.open();
		}
	}

	// Close the reading stream to prevent IO errors
	public void close() {
		if (this.open) {
			try {
				configBufferedReader.close();
				configFileReader.close();

				this.open = false;
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (configBufferedReader != null) {
				configBufferedReader = null;
			}

			if (configFileReader != null) {
				configFileReader = null;
			}
		}
	}

	// Take a string containing a variable and separate the name and value
	private Object[] parseVariable(String variableString) {
		// Create an array to hold the name (a string) and the value
		Object[] variable = null;

		int i = 0;

		// loop until the end of the string (or break
		for (/* declared 'i' above to keep it persistent */; i < variableString.length(); i++) {
			char thisChar = variableString.charAt(i);

			// If the character is an equals sign, break so that we can parse the value
			if (thisChar == '=') {
				i++;
				break;

				// If the character is an acceptable character, add it to the name portion of
				// the array
			} else if (Character.isJavaIdentifierPart(thisChar)) {

				if (variable == null) {
					variable = new Object[2];
					variable[0] = "";
				}

				variable[0] = (String) variable[0] + thisChar;
			}
		}

		// If we didn't find a name for the variable, storing the value is pointless
		if (variable != null) {
			if (variableString.length() <= i) {
				variable[1] = "";

			} else {
				String substring = variableString.substring(i);

				char firstChar = variableString.charAt(i);

				// Make sure that the character is acceptable (excludes numbers) (also needed to
				// include / and - )
				if ((Character.isJavaIdentifierPart(firstChar) && !Character.isDigit(firstChar)) || firstChar == '/'
						|| firstChar == '-') {

					// If the variable is true or false, convert it to the true or false value
					// instead of just the string representation
					if (substring.equals("true")) {
						variable[1] = true;
					} else if (substring.equals("false")) {
						variable[1] = false;

						// There shouldn't be a time when "null" shows up instead of blank space, but in
						// case it does the reader will fix it
					} else if (substring.equals("null")) {
						variable[1] = null;

						// if the value doesn't match any of the cases above, just record it as a string
						// value
					} else {
						variable[1] = variableString.substring(i);
					}

					// if the string is a number, record it as the integer representation instead of
					// a string
				} else if (Character.isDigit(variableString.charAt(i))) {
					variable[1] = toInt(substring);
					
				} else if(firstChar == '[') {
					ArrayList<String> inArray = new ArrayList<String>();
					
					int beginString = -1;
					for(int j = 1; j < substring.length(); j++) {
						char thisChar = substring.charAt(j);
						
						if(thisChar == ']') {
							if(beginString != -1) {
								inArray.add(substring.substring(beginString, j));
							}
							break;
							
						} else if(thisChar == ',') {
							if(beginString != -1) {
								inArray.add(substring.substring(beginString, j));
								beginString = -1;
							} else {
								inArray.add("");
							}
							
						} else if(Character.isJavaIdentifierPart(thisChar) || thisChar == '/' || thisChar == '-') {
							if(beginString == -1) {
								beginString = j;
							}
						}
					}
					
					variable[1] = inArray.toArray(new String[inArray.size()]);
				}
			}
		}
		
		//return the name and value we found
		return variable;
	}
	
	//Converts a string representation of a number into an int representation
	private int toInt(String string) {
		int returnInt = 0;
		
		//Loop over the characters in the string
		for (int i = 0; i < string.length(); i++) {
			//Move the number one decimal place to the left and add the current digit
			returnInt = returnInt * 10 + Character.getNumericValue((string.charAt(i)));
		}
		
		//return the integer we found
		return returnInt;
	}

	//Method to return all the configs from the file at once instead of one at a time
	public HashMap<String, Object>[] readAllConfigs() {
		//Create an empty list of hashmaps to hold the variables
		ArrayList<HashMap<String, Object>> allConfigs = new ArrayList<HashMap<String, Object>>();
		
		//open the file so that we start at the top instead of (potentially) halfway through
		this.open();
		
		//Create an array to store the current config
		HashMap<String, Object> thisConfig;
		
		//Read all configs in the file and set thisConfig to the current one
		while ((thisConfig = this.readNextConfig()) != null) {
			
			//Add the config for this loop to the list of all the configs
			allConfigs.add(thisConfig);
		}
		
		
		//Return all the configs we found in array form instead of ArrayList
		return allConfigs.toArray((HashMap<String, Object>[]) new HashMap[allConfigs.size()]);
	}

}
