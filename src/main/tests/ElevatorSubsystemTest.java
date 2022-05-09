package main.tests;

import main.core.elevatorsubsystem.ElevatorState;
import main.core.elevatorsubsystem.ElevatorStatusObj;
import main.core.elevatorsubsystem.ElevatorSubsystem;
import main.util.constants.SystemConfigConstants;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test Elevator Subsystem.
 * TESTS MUST BE RUN INDIVIDUALLY (SOCKET ERRORS).
 * THESE TESTS DEPEND ON TIME.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorSubsystemTest {

	/**
	 * Tests the flags that are set within the elevator upon receiving a message
	 * from the scheduler with errors in it.
	 *
	 * Algorithm:
	 * Make a mock scheduler that sends an error message back to the elevator.
	 * Ensure that the flag is set.
	 * Since this process takes time, wait a little bit.
	 * This flag ensures that sometime in the future, the motor error will happen.
	 */
	@Test
	public void testMotorErrorFlag() throws IOException, InterruptedException {
		ElevatorSubsystem es = new ElevatorSubsystem(0, 23);
		Thread th = new Thread(es);

		DatagramSocket sck = new DatagramSocket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
		Queue<Integer> dummyDest = new LinkedList<>();
		dummyDest.add(1);
		ElevatorStatusObj eso = new ElevatorStatusObj(new int[]{0, 1}, 0, 1, dummyDest, new HashSet<>(), ElevatorState.DOORS_OPEN, false, true);
		th.start();
		sck.receive(new DatagramPacket(new byte[100], 100));
		sck.send(eso.toPacket(23));
		Thread.sleep(100);
		assertTrue(es.getStatusObj().isMotorErrorFlag());
		sck.close();
		th.interrupt();
	}

	/**
	 * Tests the flags that are set within the elevator upon receiving a message
	 * from the scheduler with errors in it.
	 *
	 * Algorithm:
	 * Make a mock scheduler that sends an error message back to the elevator.
	 * Ensure that the flag is set.
	 * Since this process takes time, wait a little bit.
	 * This flag ensures that sometime in the future, the door error will happen.
	 */
	@Test
	public void testDoorErrorFlag() throws IOException, InterruptedException {
		ElevatorSubsystem es = new ElevatorSubsystem(0, 23);
		Thread th = new Thread(es);

		DatagramSocket sck = new DatagramSocket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
		Queue<Integer> dummyDest = new LinkedList<>();
		dummyDest.add(1);
		ElevatorStatusObj eso = new ElevatorStatusObj(new int[]{0, 1}, 0, 1, dummyDest, new HashSet<>(), ElevatorState.DOORS_OPEN, true, false);
		th.start();
		sck.receive(new DatagramPacket(new byte[100], 100));
		sck.send(eso.toPacket(23));
		Thread.sleep(100);
		assertTrue(es.getStatusObj().isDoorErrorFlag());
		sck.close();
		th.interrupt();
	}

	/**
	 * Tests the handling of the motor error state.
	 * These are timed events so they can not be tested 100%.
	 */
	@Test
	public void testHanldeMotorError() {
		ElevatorSubsystem es = new ElevatorSubsystem(0, 23);
		es.getStatusObj().setState(ElevatorState.MOTOR_ERROR);
		es.handleCurrentState();
		assertFalse(es.getRunning());
	}
}
