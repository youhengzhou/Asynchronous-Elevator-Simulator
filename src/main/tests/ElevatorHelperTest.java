package main.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import main.core.elevatorsubsystem.ElevatorHelper;

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
public class ElevatorHelperTest {

	/**
	 * Test the calculation for time take to move between floors
	 */
	@Test
	public void testCalculateTimeTo() {
		//test when traveling past a floor at max speed
		double result1 = ElevatorHelper.calculateTimeTo(false, true);
		double expect1 = 3.28;
		assertEquals(expect1,result1, 0.01);
		
		//test when stopping at the next floor at max speed 
		double result2 = ElevatorHelper.calculateTimeTo(true, true);
		double expect2 = 4.14;
		assertEquals(expect2,result2, 0.01);
		
		//test when stopping at the next floor from stop
		double result3 = ElevatorHelper.calculateTimeTo(true, false);
		double expect3 = 5;
		assertEquals(expect3,result3, 0.01);
		
		//test when stopping at the next floor at max speed 
		//and not stopping at the next floor from stop return the same;
		double result4 = ElevatorHelper.calculateTimeTo(false, false);
		assertEquals(result2, result4, 0.01);
	}

}