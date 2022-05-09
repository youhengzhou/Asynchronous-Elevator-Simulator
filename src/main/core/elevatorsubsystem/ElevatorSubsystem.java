package main.core.elevatorsubsystem;

import main.util.ThreadConsoleHelper;
import main.util.constants.AnsiConstants;
import main.util.constants.BuildingConfigConstants;
import main.util.constants.ElevatorConfigConstants;
import main.util.constants.SystemConfigConstants;

import static main.core.elevatorsubsystem.ElevatorHelper.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * This class provides an implementation for the Elevator
 * subsystem. The Elevator subsystem reads events from
 * the Scheduler and handles them accordingly.
 *
 * TODO: there is a bug with the destinations queue not having
 * the required information for the elevator (LOADING state).
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorSubsystem implements Runnable {

	/**
	 * Identifier for packets (validation)
	 */
	private static final int[] SENDERID = new int[] {0, 3};

	/**
	 * Contains the current velocity of the elevator
	 * True if moving, false if stopped
	 */
	private boolean maxVelocity;

	/**
	 * Packets for sending, receiving, and general acknowledgement
	 */
	private DatagramPacket receivePacket;

	/**
	 * Socket for sending and receiving
	 */
	private DatagramSocket sendReceiveSocket;

	/**
	 * The controller for the elevator GUI.
	 */
	private ElevatorStatusObjController controller;

	/**
	 * The object containing the status of the elevator.
	 * It has the current floor,
	 */
	private final ElevatorStatusObj statusObj;

	/**
	 * Whether the thread is to continue running or not.
	 * Set to false when a motor error happens.
	 */
	private boolean running;

	/**
	 * Default constructor for instances of Elevator.
	 *
	 * @param elevatorID The ID of this elevator (unique)
	 * @param port The port that this elevator will send/receive messages from
	 */
	public ElevatorSubsystem(int elevatorID, int port) {

		this.statusObj = new ElevatorStatusObj(SENDERID, elevatorID);

		try {
			sendReceiveSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Could not initialize socket for elevator " + elevatorID);
		}
		this.maxVelocity = false;
		this.running = true;
	}

	/**
	 *  Another constructor for instances of Elevator with controller.
	 *
	 * @param elevatorID The ID of this elevator (unique)
	 * @param port The port that this elevator will send/receive messages from
	 * @param controller The controller to be linked (if any, may be null)
	 */
	public ElevatorSubsystem(int elevatorID, int port, ElevatorStatusObjController controller) {

		this.statusObj = new ElevatorStatusObj(SENDERID, elevatorID);
		this.controller = controller;
		try {
			sendReceiveSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Could not initialize socket for elevator " + elevatorID);
		}
		this.maxVelocity = false;
		this.running = true;
	}

	/**
	 * Prints the current situation of the elevator.
	 *
	 * @param newState The new state of the Elevator
	 */
	private void printElevatorSituation(ElevatorState newState) {

		String distStr = "N/A";
		String destStr = "N/A";
		Queue<Integer> dests = statusObj.getDestinations();
		Set<Integer> destsLights = statusObj.getPanelLights();

		int currentFloor = statusObj.getFloor();
		ElevatorState state = statusObj.getState();
		if (!dests.isEmpty()) {
			destStr = String.valueOf(dests.peek());
			distStr = String.valueOf(Math.abs(dests.peek() - currentFloor));
		}

		switch (SystemConfigConstants.DEBUG_MODE) {
			case NONE:
				break;
			case DEBUG_LOW:
				List<Integer> sortedLight = new ArrayList<>(destsLights);
				Collections.sort(sortedLight);
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(),
						"is progressing through states.",
						String.format(
								"The elevator is currently on floor %d.\n" +
										"The elevator is going to floor %s.\n" +
										"The elevator is %s floor(s) away.\n" +
										"The remaining destinations are %s.\n" +
										"The panel lights that are on are %s.\n" +
										"The elevator is currently %s.\n" +
										"The elevator has been instructed %s.",
								currentFloor, destStr, distStr, dests, sortedLight, state.getRep(), newState.getSCHRep())
				);
				break;
			case DEBUG_HIGH:
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(),
						String.format("is progressing through states | %s", AnsiConstants.Green.colorize("(DBG)")),
						String.format(
								"The elevator is currently on floor %d.\n" +
										"The elevator is going to floor %s.\n" +
										"The elevator is %s floor(s) away.\n" +
										"The remaining destinations are %s.\n" +
										"The panel lights that are on are %s.\n" +
										"The current elevator state is: %s\n" +
										"The next state of the elevator will be: %s",
								currentFloor, destStr, distStr, dests, destsLights, AnsiConstants.Red.colorize(state.toString()), AnsiConstants.Red.colorize(newState.toString()))
				);
				break;
		}
	}

	/**
	 * Updates the view if there is a controller linked.
	 *
	 * @param log The log text to update with
	 */
	private void updateView(String log) {
		if (controller != null)
			controller.updateView(statusObj, log);
	}

	/**
	 * This method provides a way to switch states and make a print
	 * displaying the context of the state switch.
	 *
	 * @param newState The new state to switch to
	 */
	private void stateTransition(ElevatorState newState) {
		printElevatorSituation(newState);
		statusObj.setState(newState);
		updateView(null);
	}

	/**
	 * Sends the elevator's data to the scheduler and retrieves a message from the scheduler.
	 *
	 * @return The information retrieved from the scheduler
	 */
	private ElevatorStatusObj sendReceive() {
		ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "is sending a message to the scheduler", statusObj);
		DatagramPacket statusPckt = statusObj.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_IP,SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
		try {
			sendReceiveSocket.send(statusPckt);
		} catch (IOException e) {
			updateView("elevator could not wait to send its information to the scheduler.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait to send its information to the scheduler.", null);
			System.exit(1);
		}
		receivePacket = new DatagramPacket(new byte[100], 100);

		try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			updateView("elevator could not wait to receive information from the scheduler.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait to receive information from the scheduler.", null);
			System.exit(1);
		}
		return ElevatorStatusObj.fromPacket(receivePacket);
	}

	/**
	 * Updates the elevator's fields after receiving a message from the Scheduler.
	 *
	 * @return The message received from the elevator
	 */
	public ElevatorStatusObj updateAfterSendReceive() {
		ElevatorStatusObj recv = sendReceive();
		ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "received a message from the scheduler", recv);

		for (Integer i: recv.getPanelLights()) {
			if (!statusObj.getPanelLights().contains(i)) {
				updateView(String.format("Light %s - ON", i));
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "turning on light " + i, null);
			}
		}

		if (recv.isMotorErrorFlag()) {
			updateView("elevator received a MOTOR ERROR flag.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "received a MOTOR ERROR flag", null);
		}

		if (recv.isDoorErrorFlag()) {
			updateView("elevator received a DOOR ERROR flag.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "received a DOOR ERROR flag", null);
		}

		statusObj.clearDestinations();
		statusObj.addDestinations(recv.getDestinations());
		statusObj.clearPanelLights();
		statusObj.addPanelLights(recv.getPanelLights());

		if (!statusObj.isDoorErrorFlag())
			statusObj.setDoorErrorFlag(recv.isDoorErrorFlag());

		if (!statusObj.isMotorErrorFlag())
			statusObj.setMotorErrorFlag(recv.isMotorErrorFlag());
		return recv;
	}

	/**
	 * Handles the doors closing.
	 * Used in transition to the door closing state.
	 */
	private void handleDoorsClosing() {
		if (statusObj.isDoorErrorFlag()) {
			updateView("elevator was closing doors, but is having a door error - waiting extra time.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "was closing doors, but is having a door error - waiting extra time.", null);
			stateTransition(ElevatorState.DOOR_ERROR);
			try {
				Thread.sleep(timeToSleep(ElevatorConfigConstants.DOOR_ERROR_DOWNTIME));
			} catch (InterruptedException e) {
				updateView("elevator could not wait for its door error to be resolved.");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for its door error to be resolved.", null);
				System.exit(1);
			}
			statusObj.setDoorErrorFlag(false);
			updateView("elevator has had its door error resolved.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "has had its door error resolved.", null);
		}

		try {
			Thread.sleep(timeToSleep(ElevatorConfigConstants.DOOR_OPEN_CLOSE_TIME));
		} catch (InterruptedException e) {
			updateView("elevator could not wait for doors to close after loading passengers.");
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for doors to close after loading passengers.", null);
			System.exit(1);
		}
		stateTransition(ElevatorState.DOORS_CLOSED);
	}

	/**
	 * Provides the functionality for the handling of the current state,
	 * and switching to the next appropriate state.
	 */
	public void handleCurrentState() {
		ElevatorState currentState = statusObj.getState();
		int currentFloor = statusObj.getFloor();
		Queue<Integer> destinations = statusObj.getDestinations();
		updateView(null);

		/*
		DOORS_OPEN state:
		If the current state is doors open, check if the last destination is in play,
		if so, remove it and go back to waiting for more destinations.
		If not, but we are still at a destination, switch to the loading state.
		Else wait for the doors to close and switch to the doors closed state.
		 */
		if (currentState == ElevatorState.DOORS_OPEN) {
			while (destinations.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					updateView("elevator could not wait for additional information from the scheduler.");
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for additional information from the scheduler.", null);
					System.exit(1);
				}
				updateAfterSendReceive();
			}

			if (currentFloor == destinations.peek()) {
				destinations.poll();
				updateView(null);
				if (!destinations.isEmpty())
					stateTransition(ElevatorState.LOADING);
			} else {
				handleDoorsClosing();
			}
		}
		/*
		LOADING state:
		If we are at a destination floor, remove the destination from the queue.
		Wait for the doors to close and switch to the doors closed state.
		 */
		if (currentState == ElevatorState.LOADING) {
			try {
				Thread.sleep(timeToSleep(ElevatorConfigConstants.PASSENGER_BOARD_TIME));
			} catch (InterruptedException e) {
				updateView("elevator could not wait for passengers to board.");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for passengers to board.", null);
				System.exit(1);
			}
			handleDoorsClosing();
		}
		/*
		DOOR_CLOSED state:
		If the current destination is above switch to the moving up state.
		If the current destination is below, switch to the moving down state.
		If somehow we are on the destination floor, re-open the doors.
		For now, this last edge case will be handled with an error for debugging purposes.
		 */
		if (currentState == ElevatorState.DOORS_CLOSED) {
			if (statusObj.isMotorErrorFlag()) {
				checkMotorError();
			} else {
				if (destinations.peek() > currentFloor) {
					stateTransition(ElevatorState.MOVING_UP);
				} else if (destinations.peek() < currentFloor) {
					stateTransition(ElevatorState.MOVING_DOWN);
				} else {
					// should not happen, but if the doors are closed and we are at the destination
					// just re-open the doors of the car
					//throw new ElevatorStateProgressionError("The elevator doors were closed and the current destination was not updated.");
					try {
						Thread.sleep(timeToSleep(ElevatorConfigConstants.DOOR_OPEN_CLOSE_TIME));
					} catch (InterruptedException e) {
						updateView("elevator could not wait for doors to open after having doors closed.");
						ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for doors to open after having doors closed.", null);
						System.exit(1);
					}
					stateTransition(ElevatorState.DOORS_OPEN);
				}
			}
		}
		/*
		MOVING_UP, MOVING_DOWN states:
		Talk to the Scheduler and keep moving until the Scheduler returns a stop command.
		When at the destination floor (approaching) switch to the idle state.
		 */
		if (currentState == ElevatorState.MOVING_UP || currentState == ElevatorState.MOVING_DOWN) {

			checkMotorError();

			while (true) {
				ElevatorStatusObj recvMov = updateAfterSendReceive();
				checkMotorError();
				if (recvMov.getState() == ElevatorState.IDLE) {
					moveElevator(true, (currentState == ElevatorState.MOVING_UP));
					stateTransition(ElevatorState.IDLE);
					break;
				} else if(recvMov.getState() == ElevatorState.MOTOR_ERROR) {
					break;
				}
				else{
					moveElevator(false, (currentState == ElevatorState.MOVING_UP));
					stateTransition(recvMov.getState());
				}
			}
		}
		/*
		IDLE state:
		Wait for the doors to open.
		 */
		// TODO: the destination is being removed somewhere in the IDLE state
		if (currentState == ElevatorState.IDLE) {
			try {
				for (Iterator<Integer> iterator = statusObj.getPanelLights().iterator(); iterator.hasNext();) {
					Integer light = iterator.next();
					if (currentFloor == light) {
						updateView(String.format("Light %s - OFF", currentFloor));
						ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "turning off light " + currentFloor, null);
						iterator.remove();
					}
				}
				Thread.sleep(timeToSleep(ElevatorConfigConstants.DOOR_OPEN_CLOSE_TIME));
			} catch (InterruptedException e) {
				updateView("elevator could not wait for doors to open after being idle.");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for doors to open after being idle.", null);
				System.exit(1);
			}
			System.out.println("Elevator finished trip.");
			stateTransition(ElevatorState.DOORS_OPEN);
		}

		/*
		MOTOR_ERROR state:
		If the current state is an elevator motor error,
		the elevator should wait for a while and not exit the state.
		 */
		if (currentState == ElevatorState.MOTOR_ERROR) {
			// print the error and keep looping.
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "is currently having a motor error.", null);
			statusObj.clearDestinations();
			statusObj.clearPanelLights();
			statusObj.setDoorErrorFlag(false);
			statusObj.setMotorErrorFlag(false);
			updateView("elevator is currently having a motor error.");
			running = false;
		}
	}

	/**
	 * Executable for ElevatorSubystem threads.
	 * Progresses through the State Diagram by talking to the Scheduler.
	 * The Scheduler considers the present location and state of the Elevator and tells it what the next state
	 * should be.
	 * The Elevator considers its present state and the future state that was given to it by the Scheduler.
	 * Based on these inputs the Elevator performs the actions to handle the state transition.
	 */
	@Override
	public void run() {
		while (running) {
			/*
			It doesn't matter what state we are in.
			Attempt to retrieve destinations at any point in time.
			*/
			Queue<Integer> destinations = statusObj.getDestinations();

			ElevatorStatusObj r = updateAfterSendReceive();
			if (!statusObj.isInErrorState()) {
				while (destinations.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						updateView("elevator could not wait for additional information from the scheduler.");
						ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for additional information from the scheduler.", null);
						System.exit(1);
					}
					r = updateAfterSendReceive();
				}
			}
			handleCurrentState();
		}
		updateView("the elevator has finished execution.");
		ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "has finished execution.", null);
	}

	/**
	 * Retrieves the status object of this elevator subsystem.
	 *
	 * @return The elevator status object
	 */
	public ElevatorStatusObj getStatusObj() {
		return this.statusObj;
	}

	/**
	 * Moves the elevator one floor up or down according to its current
	 * speed and whether or not it is coming to a stop.
	 *
	 * @param stopping Whether the car is stopping or not
	 * @param up Whether the car is going up or down
	 */
	private void moveElevator(boolean stopping, boolean up) {
		// Calculate time until next floor
		double timeToNextFloor = calculateTimeTo(stopping, maxVelocity);

		// Sleep for the necessary amount of time
		try {
			Thread.sleep((long) timeToNextFloor * 1000); // waits the calculated time for the elevator to travel between floors
		} catch (InterruptedException e) {
			System.out.println("Could not wait to move.");
			System.exit(1);
		}
		
		if(up)
			statusObj.incrementFloor();
		else
			statusObj.decrementFloor();
		
		maxVelocity = true;
		
		if(stopping)
			maxVelocity = false;
	}

	/**
	 * Check whether there is a motor error
	 */
	private void checkMotorError() {
		if (statusObj.isMotorErrorFlag()) {
			System.out.println("MOTOR ERROR FLAG TRIGGERED - " + statusObj.getElevatorID());
			stateTransition(ElevatorState.MOTOR_ERROR);
			statusObj.setMotorErrorFlag(false);
		}
	}

	/**
	 * Returns whether the threads main loop will continue to execute or not.
	 *
	 * @return Main loop condition
	 */
	public boolean getRunning() {
		return running;
	}

	/**
	 * Main executable for ElevatorSubsystem.
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {

		ElevatorFrame frame = new ElevatorFrame();
		ElevatorStatusObjController controller = new ElevatorStatusObjController();
		controller.setListener(frame);
		frame.setVisible(true);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (int i = 0; i < BuildingConfigConstants.NUMBER_OF_ELEVATORS; i++) {
			Thread th = new Thread(new ElevatorSubsystem(i, SystemConfigConstants.ELEVATOR_SUBSYSTEM_PORTS[i], controller), String.format("Elevator Subsystem (%d)", i));
			th.start();
		}
	}
}
