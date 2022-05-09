package main.util.constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class provides the constants for the entire system.
 * Contains networking IPs and ports for UDP packets and sockets.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class SystemConfigConstants {

	
	public enum DebugModes{
		NONE,
		DEBUG_LOW, 
		DEBUG_HIGH
	}
	
    /**
     * Configuration file path.
     */
    private static final String CONFIG_PATH = "resources/system.config";

    /**
     * The IP address of the elevator subsystem.
     */
    public static final InetAddress ELEVATOR_SUBSYSTEM_IP;

    /**
     * The IP address of the floor subsystem.
     */
    public static final InetAddress FLOOR_SUBSYSTEM_IP;

    /**
     * The IP address of the scheduler subsystem.
     */
    public static final InetAddress SCHEDULER_SUBSYSTEM_IP;

    /**
     * The forwarding port (Floor to Elevator) of the scheduler subsystem.
     */
    public static final int SCHEDULER_SUBSYSTEM_FORWARD_PORT;

    /**
     * The backward passing port (Elevator to Floor) of the scheduler subsystem.
     */
    public static final int SCHEDULER_SUBSYSTEM_BACKWARD_PORT;

    /**
     * The starting port for the elevator subsystem, use for incremental ports.
     */
    public static final int ELEVATOR_SUBSYSTEM_START_PORT;

    /**
     * The strictly defined ports for each elevator subsystem threads.
     */
    public static final int[] ELEVATOR_SUBSYSTEM_PORTS;

    /**
     * The port of the floor subsystem.
     */
    public static final int FLOOR_SUBSYSTEM_PORT;
    
    /**
     * The int representing the level of debug print statements that will be used. 
     */
    public static final DebugModes DEBUG_MODE;

    static {
        String inputEventsFilePath = new File(SystemConfigConstants.
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
        } catch (FileNotFoundException e) {
            System.out.println("Could not read system configuration constants.");
            System.exit(1);
        }

        // attempt to parse IP addresses
        InetAddress elevIP = null;
        InetAddress floorIP = null;
        InetAddress schedIP = null;
        try {
            elevIP = InetAddress.getByName(readMap.get("ELEVATOR_SUBSYSTEM_IP"));
            floorIP = InetAddress.getByName(readMap.get("FLOOR_SUBSYSTEM_IP"));
            schedIP = InetAddress.getByName(readMap.get("SCHEDULER_SUBSYSTEM_IP"));
        } catch (UnknownHostException e) {
            System.out.println("Could not resolve system configuration IP addresses.");
            System.exit(1);
        }

        ELEVATOR_SUBSYSTEM_IP = elevIP;
        FLOOR_SUBSYSTEM_IP = floorIP;
        SCHEDULER_SUBSYSTEM_IP = schedIP;
        SCHEDULER_SUBSYSTEM_FORWARD_PORT = Integer.parseInt(readMap.get("SCHEDULER_SUBSYSTEM_FORWARD_PORT"));
        SCHEDULER_SUBSYSTEM_BACKWARD_PORT = Integer.parseInt(readMap.get("SCHEDULER_SUBSYSTEM_BACKWARD_PORT"));
        ELEVATOR_SUBSYSTEM_START_PORT = Integer.parseInt(readMap.get("ELEVATOR_SUBSYSTEM_START_PORT"));
        
        int debugMode = Integer.parseInt(readMap.get("DEBUG_MODE"));
        DEBUG_MODE = DebugModes.values()[debugMode];
        
        // get elevator ports
        String[] elevPortsStr = readMap.get("ELEVATOR_SUBSYSTEM_PORTS").split(",");
        ELEVATOR_SUBSYSTEM_PORTS = new int[elevPortsStr.length];
        for (int i = 0; i < elevPortsStr.length; i ++) {
            ELEVATOR_SUBSYSTEM_PORTS[i] = Integer.parseInt(elevPortsStr[i]);
        }

        FLOOR_SUBSYSTEM_PORT = Integer.parseInt(readMap.get("FLOOR_SUBSYSTEM_PORT"));
    }

    /**
     * Main executable for system configs.
     * Displays the system configs for each subsystem in the console.
     * Run this to ensure correctness of constants.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        System.out.printf("*ELEVATOR SUBSYSTEM - SYSTEM CONFIGS*\nip: %s\ns. port: %d\n\n", ELEVATOR_SUBSYSTEM_IP.toString(), ELEVATOR_SUBSYSTEM_START_PORT);
        System.out.printf("*FLOOR SUBSYSTEM - SYSTEM CONFIGS*\nip: %s\nport: %d\n\n", FLOOR_SUBSYSTEM_IP.toString(), FLOOR_SUBSYSTEM_PORT);
        System.out.printf("*SCHEDULER SUBSYSTEM - SYSTEM CONFIGS*\nip: %s\nf.port: %s\nb.port: %s\n", SCHEDULER_SUBSYSTEM_IP.toString(), SCHEDULER_SUBSYSTEM_FORWARD_PORT, SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
        System.out.printf("\nDEBUG_MODE: "+ DEBUG_MODE);
    }
}
