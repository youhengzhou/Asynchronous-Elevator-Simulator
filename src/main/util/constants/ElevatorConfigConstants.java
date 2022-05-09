package main.util.constants;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import main.core.floorsubsystem.FloorSubsystem;

/**
 * Elevator Timing Constants contain how long it takes for elevator to perform actions.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorConfigConstants {

	/**
	 * Configuration file path.
	 */
	private static final String CONFIG_PATH = "resources/elevator.config";

	/**
	 * Whether to use constant starting floors for all elevators or different (read from file).
	 */
	public static final boolean USE_CONSTANT_FLOORS;

	/**
	 * The starting floor for all elevators
	 */
	public static final int STARTING_FLOOR;

	/**
	 * Different starting floors for all elevators
	 */
	public static final int[] STARTING_FLOORS;

	/**
	 * The amount of time it takes to open or close the elevator car doors.
	 */
	public static final double DOOR_OPEN_CLOSE_TIME;
	
	/**
	 * The amount of time it takes for passengers to board.
	 */
	public static final double PASSENGER_BOARD_TIME;
	
	/**
	 * The amount of time it takes to travel to the next floor if the elevator is already at max velocity and not stopping at the next floor;
	 */
	public static final double FULL_SPEED_TO_FULL_SPEED;
	
	/**
	 * The amount of time it takes to travel from a full stop to stop at the next floor
	 */
	public static final double STOP_TO_STOP;
	
	/**
	 * The amount of time it takes to travel from a full stop to continue past the next floor or to go from max velocity to a complete stop at the next floor
	 */
	public static final double STOP_TO_FULL_SPEED;

	/**
	 * The amount of time it takes to recover from a door error
	 */
	public static final double DOOR_ERROR_DOWNTIME;

	/**
	 * The chance that an elevator with a motor error is fixed upon trying
	 */
	public static final double MOTOR_ERROR_FIX_CHANCE;

	/**
	 * A random generator to calculate the chance of fixing an elevator with a motor error
	 */
	private static final Random MOTOR_ERROR_RAND = new Random();

	/**
	 * Wait time for motor error
	 */
	public static final double MOTOR_ERROR_DOWNTIME;

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
				String[] spl = line.split(",", 2);
				readMap.put(spl[0], spl[1]);

			}
		} catch (Exception e) {
			System.out.println("Could not read system configuration constants.");
			System.exit(1);
		}
		
		USE_CONSTANT_FLOORS = Boolean.parseBoolean(readMap.get("USE_CONSTANT_FLOORS"));
		STARTING_FLOOR = Integer.parseInt(readMap.get("STARTING_FLOOR"));
		DOOR_OPEN_CLOSE_TIME = Double.parseDouble(readMap.get("DOOR_OPEN_CLOSE_TIME"));
		PASSENGER_BOARD_TIME = Double.parseDouble(readMap.get("PASSENGER_BOARD_TIME"));
		FULL_SPEED_TO_FULL_SPEED = Double.parseDouble(readMap.get("FULL_SPEED_TO_FULL_SPEED"));
		STOP_TO_STOP =Double.parseDouble(readMap.get("STOP_TO_STOP"));
		STOP_TO_FULL_SPEED = Double.parseDouble(readMap.get("STOP_TO_FULL_SPEED"));
		DOOR_ERROR_DOWNTIME = Double.parseDouble(readMap.get("DOOR_ERROR_DOWNTIME"));
		MOTOR_ERROR_FIX_CHANCE = Double.parseDouble(readMap.get("MOTOR_ERROR_FIX_CHANCE"));
		MOTOR_ERROR_DOWNTIME = Double.parseDouble(readMap.get("MOTOR_ERROR_DOWNTIME"));
		
		// Get starting floors for all elevators
		STARTING_FLOORS = new int[BuildingConfigConstants.NUMBER_OF_ELEVATORS];
		if (USE_CONSTANT_FLOORS) {
			Arrays.fill(STARTING_FLOORS, STARTING_FLOOR);
		}
		else{
			String[] floorsStarts = readMap.get("STARTING_FLOORS").split(",");
			for (int i = 0; i < BuildingConfigConstants.NUMBER_OF_ELEVATORS && i < floorsStarts.length; i ++) {
				STARTING_FLOORS[i] = Integer.parseInt(floorsStarts[i]);
			}
		}

	}

	/**
	 * Calculates whether a elevator with a motor error is fixed or not.
	 * Uses simulated chance.
	 * FOR FUTURE USE.
	 *
	 * @return Whether the elevator is fixed or not
	 */
	public static boolean tryFix() {
		int low = 1;
		int high = 100;
		double target = (high-low+1) * MOTOR_ERROR_FIX_CHANCE;

		int result = MOTOR_ERROR_RAND.nextInt(high-low)+low;
		return (result <= target);
	}
	
    /**
     * Main executable for elevator configs.
     * Displays the elevator configs in the console.
     * Run this to ensure correctness of constants.
     *
     * @param args Not used
     */
	public static void main(String[] args) {
		
		System.out.println("USE_CONSTANT_FLOORS: " + USE_CONSTANT_FLOORS);
		System.out.println("STARTING_FLOOR: " + STARTING_FLOOR);
		System.out.println("STARTING_FLOORS: " + Arrays.toString((int[])STARTING_FLOORS));
		System.out.println("DOOR_OPEN_CLOSE_TIME: " + DOOR_OPEN_CLOSE_TIME);
		System.out.println("PASSENGER_BOARD_TIME: " + PASSENGER_BOARD_TIME);
		System.out.println("FULL_SPEED_TO_FULL_SPEED: " + FULL_SPEED_TO_FULL_SPEED);
		System.out.println("STOP_TO_STOP: " + STOP_TO_STOP);
		System.out.println("STOP_TO_FULL_SPEED: " + STOP_TO_FULL_SPEED);
		System.out.println("DOOR_ERROR_DOWNTIME: " + DOOR_ERROR_DOWNTIME);
		System.out.println("MOTOR_ERROR_FIX_CHANCE: " + MOTOR_ERROR_FIX_CHANCE);
	}

}
