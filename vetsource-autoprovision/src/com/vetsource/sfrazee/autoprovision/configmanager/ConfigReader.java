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

	public HashMap<String, Object> readNextConfig() {
		if (!this.open) {
			this.open();
		}

		HashMap<String, Object> returnMap = null;

		try {

			configBufferedReader.reset();
			configBufferedReader.mark(255);

			String line;
			String type = null;

			while ((line = configBufferedReader.readLine()) != null) {
				char firstChar = line.charAt(0);

				if (firstChar == '|') {
					type = line.substring(1);
					break;
				}

				configBufferedReader.mark(255);
			}

			if (type != null) {
				returnMap = new HashMap<String, Object>();
				returnMap.put("configType", type);

				while ((line = configBufferedReader.readLine()) != null) {
					char firstChar = '\u0000';
					if (line.length() > 0) {
						firstChar = line.charAt(0);
					}

					if (firstChar == '|') {
						configBufferedReader.reset();
						break;

					} else if (Character.isJavaIdentifierPart(firstChar)) {
						Object[] thisVariable = parseVariable(line);

						if (thisVariable != null) {
							returnMap.put((String) thisVariable[0], thisVariable[1]);
						}
					}

					configBufferedReader.mark(255);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnMap;
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

	private Object[] parseVariable(String variableString) {
		Object[] variable = null;

		int i = 0;

		for (; i < variableString.length(); i++) {
			char thisChar = variableString.charAt(i);
			if (thisChar == '=') {
				i++;
				break;
			} else if (Character.isJavaIdentifierPart(thisChar)) {
				if (variable == null) {
					variable = new Object[2];
					variable[0] = "";
				}
				variable[0] = (String) variable[0] + thisChar;
			}
		}

		if (variable != null) {
			if (variableString.length() <= i) {
				variable[1] = "";

			} else {
				String substring = variableString.substring(i);

				char firstChar = variableString.charAt(i);
				if ((Character.isJavaIdentifierPart(firstChar) && !Character.isDigit(firstChar)) || firstChar == '/'
						|| firstChar == '-') {

					if (substring.equals("true")) {
						variable[1] = true;

					} else if (substring.equals("false")) {
						variable[1] = false;

					} else if (substring.equals("null")) {
						variable[1] = null;
					} else {
						variable[1] = variableString.substring(i);
					}

				} else if (Character.isDigit(variableString.charAt(i))) {
					variable[1] = toInt(substring);
				}
			}
		}

		return variable;
	}

	private int toInt(String string) {
		int returnInt = 0;

		for (int i = 0; i < string.length(); i++) {
			returnInt = returnInt * 10 + Character.getNumericValue((string.charAt(i)));
		}

		return returnInt;
	}

	public HashMap[] readAllConfigs() {
		ArrayList<HashMap<String, Object>> allConfigs = new ArrayList<HashMap<String, Object>>();

		this.open();
		HashMap<String, Object> thisConfig;
		while ((thisConfig = this.readNextConfig()) != null) {
			allConfigs.add(thisConfig);
		}

		return allConfigs.toArray((HashMap<String, Object>[]) new HashMap[allConfigs.size()]);
	}

}
