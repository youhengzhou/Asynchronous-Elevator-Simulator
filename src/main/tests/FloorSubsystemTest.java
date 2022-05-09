package main.tests;

import static org.junit.Assert.*;

import java.util.List;

import main.core.floorsubsystem.EventObj;
import main.core.floorsubsystem.FloorController;
import main.core.floorsubsystem.FloorSubsystem;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing class for the FloorSubSystem.
 * TESTS MUST BE RUN INDIVIDUALLY (SOCKET ISSUES)
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */

public class FloorSubsystemTest {
	
	private static final int[] SENDERID = new int[] {0, 1};
	
	/**
	 * The Floor subsystem to test
	 */
	private static FloorSubsystem fs;

	/**
	 * Initialize before each test
	 */
	@Before
	public void setup() {
		fs = new FloorSubsystem(null);
	}

	/**
	 * Testing for each input file and generate a eventQueue
	 */
	@Test
	public void testRead_data() {
		List<EventObj> eventQueue = fs.getEventQueue();
		int size = eventQueue.size();
		int expectedSize = 15;
		assertEquals(expectedSize, size);
	}

	/**
	 * Testing for reading each line and generate EventObj from string.
	 */
	@Test
	public void testRead_line() {
		EventObj expected = new EventObj(SENDERID, "0:00.00", 0, "Up", 0, 0, 0);
		EventObj result = fs.readLine("0:00.00,0,Up,0,0,0");
		assertEquals(result.getTime(),expected.getTime());
	}
}
