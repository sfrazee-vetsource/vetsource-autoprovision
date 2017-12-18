package com.vetsource.sfrazee.autoprovision.configmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ExecutableScanner {
	
	private String path;
	private boolean recursive;
	final private String[] extension = {".exe", ".msi"};
	 
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
	
	public String[] scan(String directory, String rootDirectory) {
		File scanDirectory = new File(directory);
		
		File[] initialScan = scanDirectory.listFiles();
		ArrayList<String> execPaths = new ArrayList<String>();
		
		for(int i=0; i < initialScan.length; i++) {
			
			if(initialScan[i].isFile()) {
				String fileName = initialScan[i].getName();
				if(endsWithAny(fileName, this.extension)) {
					
					if(directory.equals(rootDirectory)) {
						execPaths.add(fileName);
					} else {	
						execPaths.add(directory.substring(rootDirectory.length() + 1) + "/" + fileName);
					}
				}
				
			} else if(initialScan[i].isDirectory() && this.recursive) {
				String[] directoryScan = scan(directory + "/" + initialScan[i].getName(), rootDirectory);
				execPaths.addAll(Arrays.asList(directoryScan));
			}
		}
		
		return execPaths.toArray(new String[execPaths.size()]);
	}
	
	private boolean endsWithAny(String fileName, String[] extensions) {
		for(int i = 0; i < extensions.length; i++) {
			if(fileName.endsWith(extensions[i])) {
				return true;
			}
		}
		return false;
	}
}