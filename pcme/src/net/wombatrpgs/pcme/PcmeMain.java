/**
 *  PcmeMain.java
 *  Created on Jun 11, 2017 6:48:42 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point for PCME command line.
 */
public class PcmeMain {
	
	/**
	 * Command line entry point. See `help` method for documentation.
	 * @param	args			Command line arguments for pcme
	 */
	public static void main(String[] args) {
		ArrayList<String> argsArray = new ArrayList<String>(Arrays.asList(args));
		if (argsArray.size() != 2) {
			printHelp();
			return;
		}
		
		String inputDirectoryPath = argsArray.get(0);
		String outputFilePath = argsArray.get(1);
		
		File inputDirectory = new File(inputDirectoryPath);
		if (!inputDirectory.exists()) {
			System.err.println("Directory " + inputDirectory + " not found");
			return;
		}
		if (!inputDirectory.isDirectory()) {
			System.err.println(inputDirectory + " is not a directory");
			return;
		}
		
		File outFile = new File(outputFilePath);
		if (!outFile.exists()) {
			try {
				outFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Could not create " + outFile);
				e.printStackTrace();
				return;
			}
		}
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(outFile);
		} catch (IOException e) {
			System.err.println("Could not open " + outFile);
			e.printStackTrace();
			return;
		}
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		for (File inputFile : recursivelyFindTmxFiles(inputDirectory)) {
			System.out.println("Converting " + inputFile.getName() + "...");
			PcmeConverter converter = new PcmeConverter(inputFile);
			String output = converter.convertToDcssString();
			try {
				writer.write(output);
				writer.write("\n");
			} catch (IOException e) {
				System.err.println("IO error");
				e.printStackTrace();
			}
			
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Error closing output file");
			e.printStackTrace();
		}
		
		System.out.println("Conversion complete.");
	}
	
	/**
	 * Prints the command line help for PCME, and also doubles as its doc. It's uh pretty simple
	 * right now.
	 */
	private static void printHelp() {
		System.out.println("Usage: pcme <inputdir> <outputfile>");
		System.out.println("Converts all .tmx files from the inputdir to dcss format and prints "
				+ "them to the output file.");
	}
	
	/**
	 * 
	 * @param inputDirectory
	 * @return
	 */
	private static List<File> recursivelyFindTmxFiles(File inputDirectory) {
		ArrayList<File> tmxFiles = new ArrayList<File>();
		for (File file : inputDirectory.listFiles()) {
			if (file.isDirectory()) {
				tmxFiles.addAll(recursivelyFindTmxFiles(file));
			} else {
				if (file.getName().endsWith(".tmx")) {
					tmxFiles.add(file);
				}
			}
		}
		return tmxFiles;
	}
}
