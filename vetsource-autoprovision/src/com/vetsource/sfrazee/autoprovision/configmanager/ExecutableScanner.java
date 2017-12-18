package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ExecutableScanner {

	private String path;
	private boolean recursive;
	final private String[] extension = { ".exe", ".msi" };

	public ExecutableScanner(String path, boolean recursive) {
		this.path = path;
		this.recursive = recursive;
	}

	public ExecutableScanner(String path) {
		this.path = path;
		this.recursive = false;
	}

	public ExecutableScanner() {
		this.path = "installers";
		this.recursive = false;
	}

	public String[] scan() {
		return scan(this.path, this.path);
	}

	// Method to scan a directory (and optionally its subdirectories) for files with
	// predetermined extensions
	public String[] scan(String directory, String rootDirectory) {
		File scanDirectory = new File(directory);

		// Get an array of all the files/directories in this directory
		File[] initialScan = scanDirectory.listFiles();

		// Create an empty arraylist to hold the paths of all the executables we find
		ArrayList<String> execPaths = new ArrayList<String>();

		// Loop through all the files/directories we found
		for (int i = 0; i < initialScan.length; i++) {

			// If this item is a file and has a correct extension, add it to the list of the
			// executables we found
			if (initialScan[i].isFile()) {
				String fileName = initialScan[i].getName();
				if (endsWithAny(fileName, this.extension)) {

					if (directory.equals(rootDirectory)) {
						execPaths.add(fileName);
					} else {
						// Doing a silly thing where I'm leaving out the default root directory string,
						// but I probably shouldn't do that. I should change this later
						execPaths.add(directory.substring(rootDirectory.length() + 1) + "/" + fileName);
					}
				}

				// If this item is a directory and we want to scan recursively, then run the
				// scan operation on the directory as well
			} else if (initialScan[i].isDirectory() && this.recursive) {
				String[] directoryScan = scan(directory + "/" + initialScan[i].getName(), rootDirectory);

				// add all the files we got back to the return array as well
				execPaths.addAll(Arrays.asList(directoryScan));
			}
		}

		// Return the array representation of the paths we found
		return execPaths.toArray(new String[execPaths.size()]);
	}

	// Method for determining if the extension of the file matches any of the ones
	// we set
	private boolean endsWithAny(String fileName, String[] extensions) {

		// Loop through the list of supported extensions
		for (int i = 0; i < extensions.length; i++) {
			if (fileName.endsWith(extensions[i])) {
				return true;
			}
		}
		return false;
	}
}