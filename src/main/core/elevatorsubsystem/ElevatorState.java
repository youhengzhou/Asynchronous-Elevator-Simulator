package main.core.elevatorsubsystem;

/**
 * Enum for the ElevatorSubsystem state machine.
 * Note that in our diagram, the Arrived state and Idle state
 * are used interchangeably.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public enum ElevatorState {
	MOVING_UP ("is moving upwards", "to move upwards"),
	MOVING_DOWN("is moving downwards", "to move downwards"),
	IDLE("is idling", "to idle"),
	DOORS_CLOSED("is idling with doors closed", "to close its doors"),
	LOADING("is loading passengers", "to load passengers"),
	DOORS_OPEN("is idling with doors open", "to open its doors"),
	MOTOR_ERROR("is trying to fix a motor error", "that it has an error with its motor"),
	DOOR_ERROR("is trying to fix a door error", "that is has an error with its door");
	
	/**
	 * String representation of the state.
	 */
	private String rep;

	/**
	 * String representation of the state.
	 */
	private String schrep;

	/**
	 * Constructor for enum entries.
	 *
	 * @param rep The english representation of the state
	 * @param schrep The scheduler instruction representation of the state
	 */
	ElevatorState(String rep, String schrep) {
		this.rep = rep;
		this.schrep = schrep;
	}

	/**
	 * Getter for the String representation of the state.
	 *
	 * @return The representation of the state
	 */
	public String getRep() {
		return this.rep;
	}

	/**
	 * Getter for the String representation of the state.
	 *
	 * @return The representation of the state
	 */
	public String getSCHRep() {
		return this.schrep;
	}
}
