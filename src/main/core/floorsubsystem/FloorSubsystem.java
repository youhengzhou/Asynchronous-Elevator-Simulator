package main.core.floorsubsystem;

import main.core.scheduler.Scheduler;
import main.exceptions.FloorInitializationException;
import main.util.PacketBuilder;
import main.util.ThreadConsoleHelper;
import main.util.constants.BuildingConfigConstants;
import main.util.constants.SystemConfigConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class provides an implementation for the Floor subsystem.
 * The floor subsystem reads events from a file and pushes them
 * (one at a time) to the Scheduler.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class FloorSubsystem implements Runnable {

	/**
	 * The sender id of the floor subsystem.
	 */
	private static final int[] SENDERID = new int[] {0, 1};

	/**
	 * The relative path to the input data.
	 */
	private static final String INPUT_DATA_PATH = "resources/input_events.csv";

	/**
	 * The events that will be passed into the Scheduler.
	 */
	private final List<EventObj> eventQueue;

	/**
	 * The floors that the floorsubsystem responsible of.
	 */
	private final Floor[] floors;

	/**
	 * Packets for sending and receiving and general acknowledgement.
	 */
	private DatagramPacket receivePacket;

	/**
	 * The controller for this floor model.
	 */
	private FloorController controller;

	/**
	 * Send and receive sockets.
	 */
	private DatagramSocket sendReceiveSocket;

	/**
	 * The basic request packet when requesting a response.
	 */
	private static final DatagramPacket reqPacket;
	static {
		byte[] reqBytes = "request".getBytes(StandardCharsets.UTF_8);
		reqPacket = new DatagramPacket(reqBytes, reqBytes.length, SystemConfigConstants.SCHEDULER_SUBSYSTEM_IP, SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
	}

	/**
	 * Default constructor for instances of FloorSubsystem.
	 * Initializes a new FloorSubsystem connected to the given Scheduler,
	 * and with the data read.
	 *
	 * @param controller The controller to connect this model to views (may be null)
	 */
	public FloorSubsystem(FloorController controller) {
		setController(controller);
		int numFloors = BuildingConfigConstants.NUMBER_OF_FLOORS;
		eventQueue = new LinkedList<>();

		if (numFloors < 2)
			throw new FloorInitializationException("The number of floors that the floor subsystem contains must be greater than or equal to 2.");

		floors = new Floor[numFloors];

		floors[0] = new Floor(0, false, true);
		for (int i = 1; i < numFloors-1; i ++) {
			floors[i] = new Floor(i, true, true);
		}
		floors[numFloors-1] = new Floor(numFloors-1, true, false);

		try {
			sendReceiveSocket = new DatagramSocket(SystemConfigConstants.FLOOR_SUBSYSTEM_PORT);
		} catch (SocketException e) {
			updateViewLogs("Could not initialize socket for floor subsystem.");
			System.out.println("Could not initialize Floor Subsystem socket.");
			System.exit(1);
		}

		readData();
	}

	/**
	 * Sets the controller for this floor subsystem.
	 *
	 * @param controller The controller to set
	 */
	public void setController(FloorController controller) {
		this.controller = controller;
	}

	/**
	 * Send an update to views about the switching on or off of a light.
	 *
	 * @param floor The floor of the event
	 * @param up Whether it is the up light or down light
	 * @param on Whether the light was switched on or off
	 */
	public void updateViewLights(int floor, boolean up, boolean on) {
		if (controller != null)
			controller.updateViewLight(floor, up, on);
	}

	/**
	 * Send an update to views about a log.
	 *
	 * @param logText The new log
	 */
	public void updateViewLogs(String logText) {
		if (controller != null)
			controller.updateViewLog(logText);
	}
	
	/**
	 * This method allows the Floor subsystem to read in events
	 * from data files.
	 * 
	 * Note: The method reads files from the resources folder.
	 */
	public void readData() {
		try {
			String inputEventsFilePath = new File(FloorSubsystem.
					class.
					getClassLoader().
					getResource(INPUT_DATA_PATH).
					getFile())
					.getAbsolutePath().
					replace("%20", " ");
			
			Scanner sc = new Scanner(new File(inputEventsFilePath));
			
			while (sc.hasNext())
				eventQueue.add(readLine(sc.nextLine()));
			
			sc.close();
		} catch (FileNotFoundException e) {
			updateViewLogs("There was an error reading the input events file.");
			System.out.println("There was an error reading the input events file.");
			System.exit(1);
		}
	}
	
	/**
	 * This method reads a line from the data file and puts it into
	 * 
	 * @param line The current line of the file
	 * @return An EventObj representation of the line
	 */
	public EventObj readLine(String line) {
		String[] elem = line.split(",");
		System.out.println(elem[0]);
		return new EventObj(
				SENDERID,
				elem[0],
				Integer.parseInt(elem[1]),
				elem[2],
				Integer.parseInt(elem[3]),
				Integer.parseInt(elem[4]),
				Integer.parseInt(elem[5])
				);
	}


	/**
	 * Passes an event if there is one to pass.
	 *
	 * @param preSleepTime The time to sleep before sending the event
	 * @return If an event was passed
	 */
	private int passEvent(int preSleepTime) {
		if (!eventQueue.isEmpty()) {
			EventObj eo = eventQueue.remove(0);
			ThreadConsoleHelper.createThreadPrint(Thread.currentThread().getName(), "is pushing an event to the Scheduler", eo);
			int eventHappenTime = Integer.parseInt(eo.getTime());
			int sleepGap = eventHappenTime - preSleepTime;
			try {
				System.out.println("here we slept for " + sleepGap+"scond");
				Thread.sleep(sleepGap*1000);
			}catch(Exception e){
				e.printStackTrace();
			}
			int floor = eo.getFloor();
			int dest = eo.getCarButton();

			Floor toChange = this.floors[floor-1];

			if (floor < dest) {
				if (!toChange.isUpLight()) {
					updateViewLights(floor, true, true);
					updateViewLogs(String.format("switched the UP light ON - floor %d", floor));
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("switched the UP light ON - floor %d", floor), null);
				}
				toChange.turnOnUpLight();

			} else if (floor > dest) {
				if (!toChange.isDownLight()) {
					updateViewLights(floor, false, true);
					updateViewLogs(String.format("switched the DOWN light ON - floor %d", floor));
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("switched the DOWN light ON - floor %d", floor), null);
				}
				toChange.turnOnDownLight();
			}

			try {
				DatagramPacket eoPack = eo.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_IP, SystemConfigConstants.SCHEDULER_SUBSYSTEM_FORWARD_PORT); // make a packet whose destination is the elevator (forward)
				System.out.println(PacketBuilder.packetPrintString(eoPack, Thread.currentThread().getName(), false));
				sendReceiveSocket.send(eoPack);
				DatagramPacket recv = new DatagramPacket(new byte[100], 100);
				sendReceiveSocket.receive(recv);
				System.out.println(PacketBuilder.packetPrintString(recv, Thread.currentThread().getName(), true));
			} catch (IOException e) {
				System.out.println("Packet error when sending from floor to forwarding scheduler.");
				System.exit(1);
			}
			return eventHappenTime;
		}
		return 0;
	}

	/**
	 * Initializes a thread whose sole purpose is the passing of
	 * events to the scheduler.
	 */
	private void initPasser() {
		Thread waiter = new Thread(
				new Runnable() {
					@Override
					public void run() {
						int prevSleepTime=0;
						while (true) {
							prevSleepTime = passEvent(prevSleepTime);

						}
					}
				}, "Floor Subsystem PASSR"
		);

		waiter.start();
	}

	/**
	 * Executable for FloorSubsystem threads.
	 * Continuously attempts to push events from the FloorSubsystem to the Scheduler.
	 */
	@Override
	public void run() {

		int responses = 0;
		initPasser();

		while (true) {
			boolean recv = false;
			while (!recv) {
				try {
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "", PacketBuilder.packetToString(reqPacket, false));
					sendReceiveSocket.send(reqPacket);
				} catch (IOException e) {
					System.out.println("Could not send the request for reply packet.");
					System.exit(1);
				}

				try {
					receivePacket = new DatagramPacket(new byte[100], 100);
					sendReceiveSocket.receive(receivePacket);
					ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), "", PacketBuilder.packetToString(receivePacket, false));

					System.out.println(PacketBuilder.packetPrintString(receivePacket, Thread.currentThread().getName(), true));
					String resp = new String(receivePacket.getData()).trim();
					if (!resp.equals("NULL")) {
						EventObj eo = EventObj.fromPacket(receivePacket);
						ThreadConsoleHelper.createThreadPrint(Thread.currentThread(),"received a reply response from Scheduler", eo);
						int floor = eo.getFloor();
						int dest = eo.getCarButton();
						Floor floorObj = floors[floor-1];
						if (floor < dest) {
							if (floorObj.isUpLight()) {
								updateViewLights(floor, true, false);
								updateViewLogs(String.format("switched the UP light OFF - floor %d", floor));
								ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("switched the UP light OFF - floor %d", floor), null);
							}
							floorObj.turnOffUpLight();
						} else if (floor > dest) {
							if (floorObj.isDownLight()) {
								updateViewLights(floor, false, false);
								updateViewLogs(String.format("switched the DOWN light OFF - floor %d", floor));
								ThreadConsoleHelper.createThreadPrint(Thread.currentThread(), String.format("switched the DOWN light OFF - floor %d", floor), null);
							}
							floorObj.turnOffDownLight();
						}
						responses += 1;
						recv = true;
					}
					Thread.sleep(2000);


				} catch (IOException | InterruptedException e) {
					System.out.println("Could not receive reply packet.");
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Retrieves the event queue from the floor subsystem.
	 *
	 * @return The floor event queue
	 */
	public List<EventObj> getEventQueue(){
		return eventQueue;
	}

	/**
	 * Main executable for the FloorSubsystem.
	 * Initializes a FloorSubsystem thread and starts it.
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {
		FloorFrame ff = new FloorFrame();
		FloorController fc = new FloorController();
		fc.setListener(ff);
		ff.setVisible(true);

		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		FloorSubsystem fs = new FloorSubsystem(fc);
		Thread th = new Thread(fs, "Floor Subsystem");
		th.start();
	}
}
