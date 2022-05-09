package main.util.constants;

import main.core.floorsubsystem.FloorSubsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * System Config Constants contains the values required to initialize the system.
 * 
 * This includes number of floors and number of floors.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public final class BuildingConfigConstants {

	/**
	 * The config file path.
	 */
	private static final String CONFIG_PATH = "resources/building.config";

	/**
	 * The number of floors that the system will use.
	 */
	public static final int NUMBER_OF_FLOORS;
	
	/**
	 * The number of elevators that the system will have.
	 */
	public static final int NUMBER_OF_ELEVATORS;

	/**
	 * The distance between floors.
	 */
	public static final int FLOOR_DIST;

	static {
		String inputEventsFilePath = new File(FloorSubsystem.
				class.
				getClassLoader().
				getResource(CONFIG_PATH).
				getFile())
				.getAbsolutePath().
						replace("%20", " ");

		Map<String, String> readMap = new HashMap<>();

		try {
			Scanner sc = new Scanner(new File(inputEventsFilePath));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] spl = line.split(",");
				readMap.put(spl[0], spl[1]);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not read system configuration constants.");
			System.exit(1);
		}

		NUMBER_OF_FLOORS = Integer.parseInt(readMap.get("NUMBER_OF_FLOORS"));
		NUMBER_OF_ELEVATORS = Integer.parseInt(readMap.get("NUMBER_OF_ELEVATORS"));
		FLOOR_DIST = Integer.parseInt(readMap.get("FLOOR_DIST"));
	}

	/**
	 * Main executable for building constants.
	 * Prints the building configuration onstants that were read from the config file.
	 * Use this to ensure that the constants are correctly read.
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {
		System.out.println("NUMBER OF ELEVATORS: " + NUMBER_OF_ELEVATORS);
		System.out.println("NUMBER OF FLOORS: " + NUMBER_OF_FLOORS);
		System.out.println("FLOOR DIST: " + FLOOR_DIST);
	}
}
