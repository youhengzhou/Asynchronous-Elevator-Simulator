package main.core.scheduler;

import main.core.elevatorsubsystem.ElevatorStatusObj;
import main.core.elevatorsubsystem.ElevatorState;
import main.core.floorsubsystem.EventObj;
import main.util.ThreadConsoleHelper;
import main.util.constants.BuildingConfigConstants;
import main.util.constants.ElevatorConfigConstants;
import main.util.constants.SystemConfigConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class is an implementation of the Scheduler subsystem.
 * The Scheduler provides a means for the Floor and Elevator
 * subsystems to communicate through a buffer.
 * 
 * TODO: 
 * We need a channel for the Elevator subsystem to talk to the Scheduler and 
 * receive the next instruction.
 * 
 * We need a channel for the Floor subsystem to push events to the Scheduler.
 * We need a channel for the Elevator subsystem to receive events from the Scheduler.
 * We need a channel for the Elevator subsystem to send a reply to the Scheduler.
 * We need a channel for the Floor subsystem to retrieve a reply from the Scheduler.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class Scheduler implements Runnable {

	/**
	 * An enum of scheduler states.
	 */
	public enum SchedulerState {
		SCHEDULING_ELEVATOR,
		WAITING_ON_FLOOR_EVENT,
		WAITING_ON_ELEVATOR_HANDLE,
		PASSING_REPLY_TO_FLOOR
	}

	/**
	 * The sender id of the scheduler (for packets).
	 */
	private static final int[] SENDERID = new int[] {0, 2};

	/**
	 * Current state of the Scheduler (state diagram requirement)
	 */
	private SchedulerState currentState;

	/**
	 * Floor receive, and elevator receive packets.
	 */
	private DatagramPacket receivePacket;

	/**
	 * A socket for UDP packet communication.
	 */
	private DatagramSocket sendReceiveSocket;

	/**
	 * The total number of elevators in this simulator.
	 */
	private static final int NUM_ELEVATORS = BuildingConfigConstants.NUMBER_OF_ELEVATORS;

	/**
	 * A queue of events for the Elevators.
	 */
	private static List<EventObj> eventsQueue;

	/**
	 * A queue of reply events for the Floor subsystem.
	 */
	private static Queue<EventObj> replyQueue;

	/**
	 * A list of all scheduled events.
	 */
	private static ArrayList<EventObj>[] scheduledEvents;

	/**
	 * Start analysis time.
	 */
	private static long startTime = 0;

	/**
	 * End analysis time.
	 */
	private static long endTime = 0;

	/**
	 * Number of events processed to completion.
	 */
	private static int eventCount = 0;

	/**
	 * The amount of processed events that we calculate elapsed time at.
	 */
	private static final int MEASURE_COUNT = 15;

	/**
	 * GUI controller for Scheduler threads.
	 */
	private static SchedulerController controller = null;

	/**
	 * A list of elevator informational objects.
	 */
	private static final ElevatorStatusObj[] elevatorInfo = new ElevatorStatusObj[NUM_ELEVATORS];
	static {
		eventsQueue = new LinkedList<>();
		scheduledEvents = new ArrayList[BuildingConfigConstants.NUMBER_OF_ELEVATORS];
		replyQueue = new LinkedList<>();
		eventsQueue = new LinkedList<>();
		for (int i = 0; i < scheduledEvents.length; i ++)
			scheduledEvents[i] = new ArrayList<>();
		for (int i = 0; i < elevatorInfo.length; i ++)
			elevatorInfo[i] = new ElevatorStatusObj(SENDERID, i, ElevatorConfigConstants.STARTING_FLOORS[i], new LinkedList<>(), new HashSet<>(), ElevatorState.DOORS_OPEN, false, false);
	}

	/**
	 * Whether the scheduler is for forward reference or backward reference.
	 */
	private final boolean floorComm;

	/**
	 * Default constructor for instances of Scheduler.
	 * Initializes a new Scheduler ready to have an event pushed to it.
	 *
	 * @param floorComm Whether this Scheduler instance connects to the floor or not
	 * @param controller The controller to connect this instance to views (may be null)
	 */
	public Scheduler(boolean floorComm, SchedulerController controller) {
		Scheduler.controller = controller;
		try {
			sendReceiveSocket = new DatagramSocket((floorComm) ? SystemConfigConstants.SCHEDULER_SUBSYSTEM_FORWARD_PORT : SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
		} catch (SocketException e) {
			System.out.println("Socket could not be initialized for the Scheduler.");
			System.exit(1);
		}
		this.floorComm = floorComm;
		currentState = (floorComm) ? SchedulerState.WAITING_ON_FLOOR_EVENT : SchedulerState.WAITING_ON_ELEVATOR_HANDLE;
	}

	/**
	 * Manually set the controller at any given time.
	 *
	 * @param controller The controller to link
	 */
	public void setController(SchedulerController controller) {
		Scheduler.controller = controller;
	}

	/**
	 * Updates any views linked to the controller (if any).
	 *
	 * @param logText The log to show
	 */
	public static void updateViews(String logText) {
		if (controller != null) {
			controller.updateView(ThreadConsoleHelper.currTimestamp(), Thread.currentThread().getId(), logText);
		}
	}

	/**
	 * Sets the current state of the Scheduler.
	 * 
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(SchedulerState currentState) {
		this.currentState = currentState;
	}

	/**
	 * Function for calculating the distance between the current floor and a destination floor.
	 *
	 * @param currentFloor The current floor of the elevator
	 * @param destFloor The destination floor of the elevator
	 * @return The distance from the current floor
	 */
	private static int calcDist(int currentFloor, int destFloor) {
		return Math.abs(destFloor - currentFloor);
	}

	/**
	 * Checks if an elevator has reached either destination and updates the appropriate queues.
	 *
	 * @param elevatorID The elevator to check
	 */
	private void hasReached(int elevatorID) {

		// if its a scheduled event (i.e it hasn't reached the first destination)
		// do not consider the final destination
		// if the any elevator has reached the passenger, remove it from the scheduled events queue
		// and add it to the completing events queue (on is way to full completion)

		EventObj added = null;
		ElevatorStatusObj eso = elevatorInfo[elevatorID];
		for (Iterator<EventObj> iterator = scheduledEvents[elevatorID].iterator(); iterator.hasNext();) {
			EventObj evt = iterator.next();
			if (evt.getFloor() == eso.getFloor()) {
				added = evt;
				eso.addDestination(evt.getCarButton());
				eso.addPanelLight(evt.getCarButton());
				replyQueue.add(evt);
				eso.setDestinations(sortQueue(eso.getDestinations(), !added.getFloorButton()));
			} else if (evt.getCarButton() == eso.getFloor()) {
				eventCount ++;
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("has had '%s' events completed.", eventCount), null);
				if (eventCount == MEASURE_COUNT) {
					endTime = System.nanoTime();
					System.out.printf("The elapsed time was: %d\n", endTime-startTime);
				}
				iterator.remove();
			}
		}
	}

	/**
	 * When an object implementing interface {@code Runnable} is used
	 * to create a thread, starting the thread causes the object's
	 * {@code run} method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method {@code run} is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		/*
		One thread will attempt to receive initialization packets from each elevator.
		The Scheduler will then have baseline information for all elevators.
		 */
		if (floorComm)
			while (true) stateProgressionFS();
		else
			while (true) stateProgressionES();
	}

	/**
	 * Wrapper for scheduling.
	 * The function is synchronized just in case both threads try to access at the same time.
	 */
	private static synchronized void schedule() {
		Map<ElevatorStatusObj, List<EventObj>> rsltMap = scheduleAll(eventsQueue, elevatorInfo, scheduledEvents);
		for (ElevatorStatusObj eso: rsltMap.keySet()) {
			List<EventObj> added = rsltMap.get(eso);

			for (EventObj eo: added) {
				if (eso.isInErrorState()) {
					updateViews("scheduled an elevator in an error state.");
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "scheduled an elevator in an error state", null);
				}
				updateViews(String.format("scheduled elevator with ID %d for event going from %d to %d", eso.getElevatorID(), eo.getFloor(), eo.getCarButton()));
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("scheduled elevator with ID %d for event going from %d to %d", eso.getElevatorID(), eo.getFloor(), eo.getCarButton()), null);
			}
		}
	}

	/**
	 * Schedule all events in an event queue (maybe).
	 *
	 * @param eventQueue The queue of events to schedule
	 * @param elevatorList The list of possible elevators to schedule
	 * @param schedEvents The scheduled events for each elevator
	 * @return A map of elevators and the events that were scheduled to them
	 */
	public static Map<ElevatorStatusObj, List<EventObj>> scheduleAll(List<EventObj> eventQueue, ElevatorStatusObj[] elevatorList, List<EventObj>[] schedEvents) {

		Map<ElevatorStatusObj, List<EventObj>> resultMap = new HashMap<>();
		for (ElevatorStatusObj eso: elevatorList) {
			resultMap.put(eso, new ArrayList<>());
		}
		for (Iterator<EventObj> iterator = eventQueue.iterator(); iterator.hasNext();) {
			EventObj evt = iterator.next();
			ElevatorStatusObj result = scheduleOne(evt, elevatorList);
			if (result != null) {
				if(evt.getMotorError()){
					evt.removeMotorError();
					// continue and let this event stay in the eventQue and not be added to schedEvents.
					continue;
				}
				int elevID = result.getElevatorID();
				schedEvents[elevID].add(evt);
				resultMap.get(result).add(evt);

				if (evt.getDoorError()) {
					evt.removeDoorError();
				}

				iterator.remove();
			}
		}
		return resultMap;
	}

	/**
	 * Schedules one event for an elevator. It only changes the destinations.
	 *
	 * @param evt The event in question
	 * @param elevatorList The list of possible elevators to schedule
	 * @return The scheduled elevator object or null
	 */
	public static ElevatorStatusObj scheduleOne(EventObj evt, ElevatorStatusObj[] elevatorList) {

		int schedInd = whoToSchedule(evt, elevatorList);

		if (schedInd != -1) {
			ElevatorStatusObj eso = elevatorList[schedInd];

			Queue<Integer> updatedDestQueue = elevatorList[schedInd].getDestinations();
			//Queue<Integer> updatedDestLightQueue = elevatorList[schedInd].getDestinationsLights();
			if(!evt.getMotorError())
				updatedDestQueue.add(evt.getFloor()); //add the "start floor" of the new event to the elevator's destination list

			if (eso.getDestinations().isEmpty()) {
				elevatorList[schedInd].setDestinations(updatedDestQueue);
			} else if (evt.getFloorButton()) {
				updatedDestQueue = sortQueue(updatedDestQueue,false);
				elevatorList[schedInd].setDestinations(updatedDestQueue);
			} else {
				updatedDestQueue = sortQueue(updatedDestQueue,true);
				elevatorList[schedInd].setDestinations(updatedDestQueue);
			}

			if (evt.getDoorError()) {
				elevatorList[schedInd].setDoorErrorFlag(true);
				updateViews("has determined that elevator " + schedInd + " will have a door error.");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "has determined that elevator " + schedInd + " will have a door error.", null);
			}
			if (evt.getMotorError()) {
				elevatorList[schedInd].setMotorErrorFlag(true);
				updateViews("elevator "  + schedInd + " will get a Motor Error from event "+evt.getFloor()+" to " + evt.getCarButton()+", All events that it currently handling needs to rescheduled to other elevators");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "Elevator "  + schedInd + " will get a Motor Error from event "+evt.getFloor()+" to " + evt.getCarButton()+", All events that it currently handling needs to rescheduled to other elevators","" );
			}
			return elevatorList[schedInd];
		}
		return null;
	}


	/**
	 * Determines what elevator should be scheduled for the given task.
	 *
	 * @param evt The event to schedule
	 * @param elevatorList The list of elevators to check
	 * @return The index of the elevator to schedule or -1
	 */
	public static int whoToSchedule(EventObj evt, ElevatorStatusObj[] elevatorList)
	{
		//find all elevator with no destinations
		int minDist = 99;
		int minDistID =-1;
				//loop through len(elavatorList) time
				//find elevator with no destinations
				//
		for (ElevatorStatusObj esm : elevatorList)
		//we are looping though all elevtors
		// if one elevator is in Error state, we are NOT going to look at it, and skip it.
		{
			//check if the esm elevator is in Error state,
			// if true , we continue;
			if (esm.isInErrorState())
				continue;

			if (esm.getDestinations().isEmpty()) //we have a elevator having No destination.
			{
				int distFromFloor = calcDist(esm.getFloor(), evt.getFloor());
				//  	                         elevator's floor(5),    the floor passenger is at(3).
				if(minDist==99) {
					minDist = distFromFloor;
					minDistID = esm.getElevatorID();
				}
				else if(distFromFloor<minDist){
					minDist = distFromFloor;
					minDistID = esm.getElevatorID();
					//elevator's with minDistance
				}
			}
		}

		if (minDist == 99) {

			if (evt.getFloorButton()) {

				// moving up event
				// logic: find an elevator that are moving up, that are also closest to the passenger's floor
				for (ElevatorStatusObj esm : elevatorList) {
					if(esm.isInErrorState())
						continue;

					// this string variable is to prevent edge case
					// where the elevator is moving UP, but it's actually going up to pick up a DOWN event
					// make sure that it's going up for the right reason
					String typeOfEventElevatorCurrentlyHandling = getTypeOfEventCurrentlyHandling(esm);

					if (esm.getDestinations().peek() > esm.getFloor() && esm.getFloor() < evt.getFloor() && typeOfEventElevatorCurrentlyHandling.equals("UP")) {
						int distFromFloor = Math.abs(esm.getFloor() - evt.getFloor());
						if (minDist == 99) {
							minDist = distFromFloor;
							minDistID = esm.getElevatorID();
						} else if (distFromFloor < minDist) {
							minDist = distFromFloor;
							minDistID = esm.getElevatorID();
						}
					}
				}
			} else {
				// moving down event
				// logic: find an elevator that are moving down, that are also closest to the passenger's floor

				for (ElevatorStatusObj esm : elevatorList) {
					if(esm.isInErrorState())
						continue;

					String typeOfEventElevatorCurrentlyHandling = getTypeOfEventCurrentlyHandling(esm);
					if (esm.getDestinations().peek() < esm.getFloor() && esm.getFloor() > evt.getFloor() && typeOfEventElevatorCurrentlyHandling.equals("DOWN")) {
						int distFromFloor = Math.abs(esm.getFloor() - evt.getFloor());
						if (minDist == 99) {
							minDist = distFromFloor;
							minDistID = esm.getElevatorID();
						} else if (distFromFloor < minDist) {
							System.out.println(esm.getElevatorID() + "is scheduled" );
							minDist = distFromFloor;
							minDistID = esm.getElevatorID();
						}
					}
				}
			}
		}

		return minDistID;
	}

	/**
	 * Based on the first destination that the elevator currently is going to, we want to know if this elevator is going up or down
	 *
	 * @param esm The status of a particular elevator
	 * @return What type of event the elevator is currently handling
	 */
	public static String getTypeOfEventCurrentlyHandling(ElevatorStatusObj esm){

		// In the first half, we checking based on the "START floor" of an event;
		EventObj eventHanding = scheduledEvents[esm.getElevatorID()].stream()
				.filter(e -> e.getFloor()==esm.getDestinations().peek())
				.findFirst()
				.orElse(null);
		String typeOfEventElevatorCurrentlyHandling = "NA";
		if(eventHanding!=null){
			typeOfEventElevatorCurrentlyHandling = eventHanding.getFloorButton() ? "UP" : "DOWN";
			return typeOfEventElevatorCurrentlyHandling;
		}

		// In the second half, we checking based on the "ACTUAL destination floor" of an event;
		eventHanding = scheduledEvents[esm.getElevatorID()].stream()
				.filter(e -> e.getCarButton()==esm.getDestinations().peek())
				.findFirst()
				.orElse(null);
		typeOfEventElevatorCurrentlyHandling = "NA";
		if(eventHanding!=null){
			typeOfEventElevatorCurrentlyHandling = eventHanding.getFloorButton() ? "UP" : "DOWN";
		}
		return typeOfEventElevatorCurrentlyHandling;
	}

	/**
	 * Sort destination queue and remove duplications.
	 *
	 * @param queue The queue to sort
	 * @param isMovingDown Whether the elevator is moving down or not (up otherwise)
	 * @return The sorted queue
	 */
	private static Queue<Integer> sortQueue(Queue<Integer> queue, boolean isMovingDown)
	{
		Set<Integer> newSet = new HashSet<>(queue);
		LinkedList myList = new LinkedList(newSet);
		Collections.sort(myList);
		Collections.sort(myList);
		if(isMovingDown) Collections.reverse(myList);
		return new LinkedList<>(myList);
	}

	/**
	 * State progression for the Elevator Subsystem thread.
	 */
	private void stateProgressionES() {
		switch (currentState) {
			case SCHEDULING_ELEVATOR:
			case WAITING_ON_FLOOR_EVENT:
				break;
			case WAITING_ON_ELEVATOR_HANDLE:
				receivePacket = new DatagramPacket(new byte[100], 100);
				try {
					sendReceiveSocket.receive(receivePacket);
					String req = new String(receivePacket.getData()).trim();

					// if the packet was a request, then switch states and service it
					if (req.equals("request")) {
						currentState = SchedulerState.PASSING_REPLY_TO_FLOOR;
						// if not a request, then it would be elevator data
					} else {
						schedule();
						setCurrentState(SchedulerState.WAITING_ON_ELEVATOR_HANDLE);
						ElevatorStatusObj esm = ElevatorStatusObj.fromPacket(receivePacket);

						int elevID = esm.getElevatorID(); // this is who sent the packet to the scheduler
						if (elevatorInfo[elevID] == null)
							elevatorInfo[elevID] = new ElevatorStatusObj(SENDERID, esm.getElevatorID(), esm.getFloor(), esm.getDestinations(), esm.getPanelLights(), esm.getState(), esm.isDoorErrorFlag(), esm.isMotorErrorFlag());
						ElevatorStatusObj storedEsm = elevatorInfo[elevID];
						storedEsm.setState(esm.getState());

						if (esm.isInErrorState()) {
							updateViews(String.format("detected that elevator (%s) is in an error state.", elevID));
							ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("detected that elevator (%s) is in an error state", elevID), null);
						}

						storedEsm.setFloor(esm.getFloor());

						// compare the panelLights stored in scheduler vs panelLights in the new status obj.
						// remove light, to sync with the elevator's new request's light
						storedEsm.getPanelLights().removeIf(light -> !esm.getPanelLights().contains(light));

						boolean removeDest = false;

						// attempt to put back events that have been scheduled to the motor error
						if (storedEsm.getState() == ElevatorState.MOTOR_ERROR) {
							updateViews(String.format("must reschedule events for elevator (%s).", elevID));
							ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("must reschedule events for elevator (%s)", elevID), null);

							Map<Integer,Integer> putBackEventMap = new HashMap<>();
							Queue<Integer> destinations = storedEsm.getDestinations();
							for (Integer i : destinations) {
								putBackEventMap.put(i, storedEsm.getElevatorID());
							}

							for (Integer floor : putBackEventMap.keySet())
							{
								boolean rescheduledSuccess = putBackToEvtQueue(floor, putBackEventMap.get(floor));
								if (!rescheduledSuccess) {
									eventCount++;
									updateViews(String.format("did not reschedule event going to floor %s", floor));
									ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("did not reschedule event going to floor %s", floor), null);
								}
							}
							putBackEventMap.keySet()
									.forEach(e -> {
										putBackToEvtQueue(e, putBackEventMap.get(e));
										elevatorInfo[putBackEventMap.get(e)].clearDestinations();
									});

						} else {
							if (!storedEsm.getDestinations().isEmpty()) {
								int currentFloor = storedEsm.getFloor();
								int currentDestination = storedEsm.getDestinations().peek();
								int dist = calcDist(currentFloor, currentDestination);

								/*
								if the distance is one and we are currently moving,
								signal the elevator to stop with the IDLE state.

								if we are 0 floors away, then check for any completed
								or semi-completed events.
								in this case, if we are in the doors open state,
								remove the current destination after it has been
								removed from the elevator (after message is sent).
								 */
								if (dist == 1 && storedEsm.isMoving()) {
									storedEsm.setState(ElevatorState.IDLE);
								} else if (dist == 0) {
									hasReached(elevID);
									if (storedEsm.getState() == ElevatorState.DOORS_OPEN)
										removeDest = true;
								}
							}
						}
						DatagramPacket storedEsmPack = storedEsm.toPacket(SystemConfigConstants.ELEVATOR_SUBSYSTEM_IP, receivePacket.getPort());
						sendReceiveSocket.send(storedEsmPack); // ack packet is the stored data (may have new destinations)

						// stop flags from being applied twice
						if (storedEsm.isDoorErrorFlag()) {
							updateViews(String.format("sent a door error flag to elevator (%s).", elevID));
							storedEsm.setDoorErrorFlag(false);
						}
						if (storedEsm.isMotorErrorFlag()) {
							updateViews(String.format("sent a motor error flag to elevator (%s).", elevID));
							storedEsm.setMotorErrorFlag(false);
						}

						// remove the destination after
						if (removeDest)
							storedEsm.getDestinations().poll();
					}
				} catch (IOException e) {
					updateViews("could not receive elevator packet.");
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not receive elevator packet", null);
					System.exit(1);
				}
				break;
			case PASSING_REPLY_TO_FLOOR:
				try {
					DatagramPacket reply;
					// packets available
					if (!replyQueue.isEmpty())
						reply = replyQueue.poll().toPacket(SystemConfigConstants.FLOOR_SUBSYSTEM_IP, receivePacket.getPort());
					// no packets to forward
					else
						reply = makeNegAckPacket(SystemConfigConstants.FLOOR_SUBSYSTEM_IP, receivePacket.getPort());

					sendReceiveSocket.send(reply);
				} catch (IOException e) {
					updateViews("could not wait to send a reply to floor.");
					System.out.println("Could not send reply to floor.");
					System.exit(1);
				}
				currentState = SchedulerState.WAITING_ON_ELEVATOR_HANDLE;
				break;
			default:
				updateViews("incorrect state progression detected.");
				System.out.println("There was incorrect state progression.");
				System.exit(1);
				break;
		}
	}

	/**
	 * State progression for the Floor Subsystem thread.
	 */
	private void stateProgressionFS() {

		switch (currentState) {
			case WAITING_ON_FLOOR_EVENT: {
				receivePacket = new DatagramPacket(new byte[100], 100);
				try {
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					updateViews("could not wait for a floor event to be received.");
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "could not wait for a floor event to be received.", null);
					System.exit(1);
				}

				EventObj eo = EventObj.fromPacket(receivePacket);
				eventsQueue.add(eo);
				updateViews("received an event from floor subsystem.");
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "received event from floor subsystem", eo);

				// the first event was received so get start time.
				if (eventCount == 0)
					startTime = System.nanoTime();

				DatagramPacket ackPacket = makeAckPacket(SystemConfigConstants.FLOOR_SUBSYSTEM_IP, receivePacket.getPort());
				try {
					sendReceiveSocket.send(ackPacket);
				} catch (IOException e) {
					updateViews("could not wait to send the floor a reply.");
					System.out.println("Could not wait to send the floor reply.");
					System.exit(1);
				}
				setCurrentState(SchedulerState.SCHEDULING_ELEVATOR);
				break;
			}
			case SCHEDULING_ELEVATOR: {
				// Scheduling done here - choose the best elevator based on knowledge and new destinations
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				schedule();
				setCurrentState(SchedulerState.WAITING_ON_FLOOR_EVENT);
				break;
			}
			case WAITING_ON_ELEVATOR_HANDLE:
			case PASSING_REPLY_TO_FLOOR:
				break;
			default:
				updateViews("incorrect state progression detected.");
				System.out.println("There was incorrect state progression.");
				System.exit(1);
				break;
		}
	}

	/**
	 * Makes a negative acknowledgement packet.
	 *
	 * @param host The destination host
	 * @param port The destination port
	 * @return The packet
	 */
	private DatagramPacket makeNegAckPacket(InetAddress host, int port) {
		byte[] negAck = "NULL".getBytes(StandardCharsets.UTF_8);
		return new DatagramPacket(negAck, negAck.length, host, port);
	}

	/**
	 * Makes a positive acknowledgement packet.
	 *
	 * @param host The destination host
	 * @param port The destination port
	 * @return The packet
	 */
	private DatagramPacket makeAckPacket(InetAddress host, int port) {
		byte[] ack = "ACK".getBytes(StandardCharsets.UTF_8);
		return new DatagramPacket(ack, ack.length, host, port);
	}

	/**
	 * will put the event starting from "floor", that are original in scheduledQueue, we find it based on "floor"
	 * and put it back to evtQueue.
	 *
	 * @param floor The floor to check events on
	 */
	private boolean putBackToEvtQueue(int floor, int elevatorID){
		//find the event in schedule Queue
		// remove it from scheduled queue
		// put it in evtQueue.
		boolean scheduledSuccess = false;
		for (int i = 0; i<scheduledEvents[elevatorID].size(); i++) {
			if (scheduledEvents[elevatorID].get(i).getFloor() == floor) {
				EventObj result = scheduledEvents[elevatorID].get(i);
				updateViews(String.format("has determined that based on floor %s, the event [%s, %s] will be placed back in the event queue (reschedule)", floor, result.getFloor(), result.getCarButton()));
				ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("has determined that based on floor %s, the event [%s, %s] will be placed back in the event queue (reschedule)", floor, result.getFloor(), result.getCarButton()), null);
				scheduledEvents[elevatorID].remove(result);
				eventsQueue.add(result);
				scheduledSuccess = true;
			}
		}
		return scheduledSuccess;
	}

	/**
	 * Main executable for Scheduler.
	 * Makes two threads that control the flow of different channels.
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {
		/*
		Create two threads:
		1) for the communication between scheduler and elevator
		2) for the communication between floor and scheduler
		 */
		SchedulerController sc = new SchedulerController();
		SchedulerFrame sf = new SchedulerFrame();
		sc.setListener(sf);
		sf.setVisible(true);

		Thread flSCThread = new Thread(new Scheduler(true, sc), "Scheduler - Floor communicator thread");
		Thread elSCThread = new Thread(new Scheduler(false, sc), "Scheduler - Elevator communication thread");

		flSCThread.start();
		elSCThread.start();
	}
}
