package main.tests;

import static org.junit.Assert.*;

import main.core.elevatorsubsystem.ElevatorStatusObj;
import main.core.elevatorsubsystem.ElevatorState;
import main.core.floorsubsystem.EventObj;
import main.core.scheduler.Scheduler;
import main.util.constants.SystemConfigConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Testing class for the Scheduler.
 * Tests the functions asynchronously.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class SchedulerTest {

	private static final int[] SENDERID = new int[] {0, 1};

	
	/**
	 * The testing Event object.
	 */
	private static EventObj eo;
	private static EventObj eo1;
	private static EventObj eo2;
	private static EventObj eo3;
	private static EventObj eo4;
	private static EventObj eo5;
	private static EventObj eo6;
	private static EventObj eo7;
	
	@BeforeClass
	/**
	 * Set up events that are used for shceduling and assigning
	 */
	public static void setup() {
		eo = new EventObj(SENDERID,"0:00.00", 1, "Up", 5, 0, 0);
		eo1 = new EventObj(SENDERID,"0:00.00", 2, "Up", 4, 0, 0);
		eo2= new EventObj(SENDERID,"0:00.00", 1, "Up", 3, 0, 0);
		eo3 = new EventObj(SENDERID,"0:00.00", 4, "Down", 1, 0, 0);
		eo4 = new EventObj(SENDERID,"0:00.00", 5, "Down", 2, 0, 0);
		eo5 = new EventObj(SENDERID,"0:00.00", 4, "Up", 5, 0, 0);
		eo6 = new EventObj(SENDERID,"0:00.00", 2, "Down", 1, 0, 0);
		eo7 = new EventObj(SENDERID,"0:00.00", 3, "Up", 5, 0, 0);
	}

	@Test
	/**
	 * Testing scheduling an event when two elevators are idle
	 */
	public void testSchedule1() {
		//CASE 1: Two elevator are both Idle, the new event wll go to the one that are closer.
		LinkedList<EventObj> evtQueue = new LinkedList<>();
		List<EventObj>[] schedList = new ArrayList[2];
		Arrays.fill(schedList, new ArrayList<>());

		// initialize esm instances and add them to array.
		LinkedList<Integer> destQueue1 = new LinkedList<>();
		LinkedList<Integer> destQueue2 = new LinkedList<>();

		ElevatorStatusObj esm1 = new ElevatorStatusObj(new int[]{0, 3}, 0, 4,destQueue1,new HashSet<>(),ElevatorState.IDLE);
		ElevatorStatusObj esm2 = new ElevatorStatusObj(new int[]{0, 3}, 1, 3,destQueue2,new HashSet<>(),ElevatorState.IDLE);

		ElevatorStatusObj[] esmList= {esm1, esm2};
		evtQueue.add(eo); //1,Up
		evtQueue.add(eo4); //5,down
		Scheduler.scheduleAll(evtQueue,esmList, schedList);

		Queue<Integer> destQueueExpect1 = new LinkedList<>(); // expected list value
		destQueueExpect1.add(1);

		Queue<Integer> destQueueExpect2 = new LinkedList<>();
		destQueueExpect2.add(5);

		// Expected result: the elevator at floor 3 receive a new destination going to floor 1;
		assertEquals(esmList[1].getDestinations(), destQueueExpect1);
		// Expected result: the elevator at floor 4 receive a destination going ot floor 5;
		assertEquals(esmList[0].getDestinations(), destQueueExpect2);
	}

	/**
	 * Testing schedule an event to two potential elevator that are both moving up or going to move up.
	 */
	@Test
	public void testSchedule2() {
		//CASE 2: Two elevator are both going to move up or moving up, the new event wll go to the one that are closer.
		List<EventObj>[] schedList = new ArrayList[2];
		Arrays.fill(schedList, new ArrayList<>());
		LinkedList<EventObj> evtQueue = new LinkedList<>();
		// initialize esm instances and add them to array.
		LinkedList<Integer> destQueue1 = new LinkedList<>();
		LinkedList<Integer> destQueue2 = new LinkedList<>();

		ElevatorStatusObj esm1 = new ElevatorStatusObj(new int[]{0, 3}, 0, 1,destQueue1,new HashSet<>(),ElevatorState.DOORS_OPEN);
		ElevatorStatusObj esm2 = new ElevatorStatusObj(new int[]{0, 3}, 1, 3,destQueue2,new HashSet<>(),ElevatorState.MOVING_UP);

		ElevatorStatusObj[] esmList= {esm1, esm2};
		evtQueue.add(eo5); //4,Up
		Scheduler.scheduleAll(evtQueue, esmList, schedList);

		Queue<Integer> destQueueExpect1 = new LinkedList<>(); // expected list value

		Queue<Integer> destQueueExpect2 = new LinkedList<>();
		destQueueExpect2.add(4);

		System.out.println(Scheduler.whoToSchedule(eo5, esmList));

		// Expected result: the elevator at floor 1 is not receiving anything;
		System.out.println(esmList[0].getDestinations());
		assertEquals(esmList[0].getDestinations(), destQueueExpect1);
		// Expected result: the elevator at floor 3 receive a new destination going to floor 4;
		System.out.println(esmList[1].getDestinations());
		assertEquals(esmList[1].getDestinations(), destQueueExpect2);

	}

	/**
	 * Testing for scheduling event for two potential elevator that are moving down or going to move down.
	 */
	@Test
	public void testSchedule3() {
		//CASE 3: Two elevator are both going to move down or moving down, the new event wll go to the one that are closer.
		List<EventObj>[] schedList = new ArrayList[2];
		Arrays.fill(schedList, new ArrayList<>());
		LinkedList<EventObj> evtQueue = new LinkedList<>();
		// initialize esm instances and add them to array.
		LinkedList<Integer> destQueue1 = new LinkedList<>();
		LinkedList<Integer> destQueue2 = new LinkedList<>();

		ElevatorStatusObj esm1 = new ElevatorStatusObj(new int[]{0, 3}, 0, 5,destQueue1,new HashSet<>(),ElevatorState.DOORS_OPEN);
		ElevatorStatusObj esm2 = new ElevatorStatusObj(new int[]{0, 3}, 1, 4,destQueue2,new HashSet<>(),ElevatorState.MOVING_UP);

		ElevatorStatusObj[] esmList= {esm1, esm2};
		evtQueue.add(eo6); //2,down
		Scheduler.scheduleAll(evtQueue,esmList, schedList);

		Queue<Integer> destQueueExpect1 = new LinkedList<>(); // expected list value

		Queue<Integer> destQueueExpect2 = new LinkedList<>();
		destQueueExpect2.add(2);

		assertEquals(esmList[0].getDestinations(), destQueueExpect1);
		assertEquals(esmList[1].getDestinations(), destQueueExpect2);

	}

	@Test
	/**
	 * Testing for no best-fit elevator for current event
	 */
	public void testSchedule4() {
		//CASE 4: no elevator is a good fit for the current event, scheduleSingleEvent should return false
		// when an event scheduled successful, scheduleSingleEvent should return true
		LinkedList<EventObj> evtQueue = new LinkedList<>();
		List<EventObj>[] schedList = new ArrayList[2];
		Arrays.fill(schedList, new ArrayList<>());
		// initialize esm instances and add them to array.
		LinkedList<Integer> destQueue1 = new LinkedList<>();
		destQueue1.add(5);
		LinkedList<Integer> destQueue2 = new LinkedList<>();
		destQueue2.add(4);

		ElevatorStatusObj esm1 = new ElevatorStatusObj(new int[]{0, 3}, 0, 2,destQueue1,new HashSet<>(),ElevatorState.DOORS_OPEN);
		ElevatorStatusObj esm2 = new ElevatorStatusObj(new int[]{0, 3}, 1, 3,destQueue2,new HashSet<>(),ElevatorState.MOVING_UP);

		ElevatorStatusObj[] esmList= {esm1, esm2};

		assertEquals(Scheduler.whoToSchedule(eo, esmList), -1); // no elevator can serve event from F1 and going up
	}

	/**
	 * Integration test for that contains potential elevators in multiple states.
	 */
	@Test
	public void testSchedule() {
		//Integration test for schedule.
		LinkedList<EventObj> evtQueue = new LinkedList<>();
		List<EventObj>[] schedList = new ArrayList[4];
		Arrays.fill(schedList, new ArrayList<>());
		// initialize esm instances and add them to array.
		LinkedList<Integer> destQueue1 = new LinkedList<>();
		LinkedList<Integer> destQueue2 = new LinkedList<>();
		LinkedList<Integer> destQueue3 = new LinkedList<>();
		LinkedList<Integer> destQueue4 = new LinkedList<>();

		ElevatorStatusObj esm1 = new ElevatorStatusObj(new int[]{0, 3}, 0, 5,destQueue1,new HashSet<>(),ElevatorState.DOORS_OPEN);
		ElevatorStatusObj esm2 = new ElevatorStatusObj(new int[]{0, 3}, 1, 1,destQueue2,new HashSet<>(),ElevatorState.MOVING_UP);
		ElevatorStatusObj esm3 = new ElevatorStatusObj(new int[]{0, 3}, 2, 2,destQueue3,new HashSet<>(),ElevatorState.MOVING_UP);
		ElevatorStatusObj esm4 = new ElevatorStatusObj(new int[]{0, 3}, 3, 3,destQueue4,new HashSet<>(),ElevatorState.MOVING_DOWN);

		ElevatorStatusObj[] esmList= {esm1, esm2,esm3,esm4};
		evtQueue.add(eo3);
		evtQueue.add(eo7);
		evtQueue.add(eo6);
		Scheduler.scheduleAll(evtQueue,esmList, schedList);

		Queue<Integer> destQueueExpect1 = new LinkedList<>(); // expected list value
		destQueueExpect1.add(4);
		Queue<Integer> destQueueExpect2 = new LinkedList<>();
		destQueueExpect2.add(2);
		Queue<Integer> destQueueExpect3 = new LinkedList<>();
		destQueueExpect3.add(3);
		assertEquals(esmList[0].getDestinations(), destQueueExpect1);
		assertEquals(esmList[2].getDestinations(), destQueueExpect2);
		assertEquals(esmList[3].getDestinations(), destQueueExpect3);
	}
}
