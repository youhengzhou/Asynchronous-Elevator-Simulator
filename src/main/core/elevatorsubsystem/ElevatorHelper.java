package main.core.elevatorsubsystem;

import main.util.constants.ElevatorConfigConstants;

/**
 * This class provides an interface containing methods and fields
 * pertaining to the status of Elevators in the Elevator Subsystems.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorHelper {
	
	/**
	 * This method provides an Elevator in the Elevator subsystem
	 * the time it will take to get to the next floor
	 * 
	 * @param stopping true if the elevator is stopping at the next floor, false otherwise
	 * @param maxVelocity true if the elevator is traveling at max velocity (1.216 m/s), false if the elevator is currently stopped
	 * @return The time it would take the Elevator to travel from the current floor to the destination floor
	 */
	public static double calculateTimeTo(boolean stopping, boolean maxVelocity) {
		
		double t;
		
		// If the elevator is stopping at the next floor and currently at max velocity 
		// or if the elevator is not stopping at the next floor and is currently stopped
		if(stopping && maxVelocity || !stopping && !maxVelocity) {
			t = ElevatorConfigConstants.STOP_TO_FULL_SPEED;
		}
		// If the elevator is stopping at the next floor and currently stopped 
		else if(stopping && !maxVelocity) {
			t = ElevatorConfigConstants.STOP_TO_STOP;
		}
		// If the elevator is not stopping and and is at max velocity
		else {
			t = ElevatorConfigConstants.FULL_SPEED_TO_FULL_SPEED;
		}
		
		return t;
	}

	/**
	 * Easy method for the conversion from time in seconds to sleep time.
	 *
	 * @param time The time to sleep for (in seconds/double)
	 * @return The time to sleep for (in milliseconds/long)
	 */
	public static long timeToSleep(double time) {
		return (long) time * 1000;
	}
}
