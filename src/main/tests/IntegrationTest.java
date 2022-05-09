package main.tests;

import main.core.elevatorsubsystem.ElevatorState;
import main.core.elevatorsubsystem.ElevatorStatusObj;
import main.core.floorsubsystem.EventObj;
import main.core.scheduler.Scheduler;
import main.util.constants.SystemConfigConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * Tests the integration of all features together.
 * Uses sockets so all tests must be run individually.
 *
 * FOR THESE TESTS TO WORK, THE BUILDING CONFIGURATION FILE MUST
 * ALLOW UP TO 4 ELEVATORS.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class IntegrationTest {

    /**
     * The necessary testing objects.
     */
    private static final int[] SENDERID = new int[] {0, 1};
    private static EventObj eo1;
    private static EventObj eo2;
    private static ElevatorStatusObj eso1;
    private static ElevatorStatusObj eso2;
    private static ElevatorStatusObj eso3;
    private static ElevatorStatusObj eso4;

    /**
     * The basic request packet when requesting a response.
     */
    private static final DatagramPacket reqPacket;
    static {
        byte[] reqBytes = "request".getBytes(StandardCharsets.UTF_8);
        reqPacket = new DatagramPacket(reqBytes, reqBytes.length, SystemConfigConstants.SCHEDULER_SUBSYSTEM_IP, SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
    }

    /**
     * Setup to be done before all the tests are run.
     */
    @BeforeClass
    public static void setup() {
        eo1 = new EventObj(SENDERID,"0:00.00", 2, "Up", 4, 0, 0);
        eo2= new EventObj(SENDERID,"0:00.00", 1, "Up", 3, 0, 0);

        Queue<Integer> destsESO2 = new LinkedList<>();
        destsESO2.add(6);

        eso1 = new ElevatorStatusObj(SENDERID, 0, 1, new LinkedList<>(), new HashSet<>(), ElevatorState.DOORS_OPEN, false, false);
        eso2 = new ElevatorStatusObj(SENDERID, 1, 5, destsESO2, new HashSet<>(), ElevatorState.MOVING_UP, false, false);
        eso3 = new ElevatorStatusObj(SENDERID, 2, 7, destsESO2, new HashSet<>(), ElevatorState.MOVING_DOWN, false, false);
        eso4 = new ElevatorStatusObj(SENDERID, 3, 1, new LinkedList<>(), new HashSet<>(), ElevatorState.MOTOR_ERROR, false, false);
    }

    /**
     * Test the scheduler's integration with the floor subsystem.
     *
     * Tests:
     * Send an event object to the scheduler (pretending to be the floor subsystem)
     * Wait for an acknowledgement back from the scheduler (pretending to be the floor subsystem)
     * Ensure the response is accurate.
     *
     * THIS TEST MAY CONFLICT WITH OTHERS DUE TO SOCKET INITIALIZATION.
     *
     * @throws IOException Socket-related errors
     */
    @Test
    public void testSchedulerIntegrationFloor() throws IOException {
        Scheduler sc = new Scheduler(true, null);
        Thread sct = new Thread(sc, "Scheduler - Testing Floor Comm");

        DatagramSocket socket = new DatagramSocket();

        DatagramPacket sendPacket = eo1.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_FORWARD_PORT);
        byte[] receiveData = new byte[10];
        DatagramPacket recvPacket = new DatagramPacket(receiveData, receiveData.length);

        sct.start();

        socket.send(sendPacket);

        socket.receive(recvPacket);

        assertEquals(new String(receiveData).trim(), "ACK");

        socket.close();
    }

    /**
     * Test the scheduler's integration with the floor subsystem.
     *
     * Tests:
     * Send a request for a reply from the scheduler (pretending to be the floor subsystem).
     * Check if the response is valid.
     *
     * THIS TEST MAY CONFLICT WITH OTHERS DUE TO SOCKET INITIALIZATION.
     *
     * @throws IOException Socket-related errors
     */
    @Test
    public void testSchedulerIntegrationFloor2() throws IOException {
        Scheduler sc = new Scheduler(false, null);
        Thread sct = new Thread(sc, "Scheduler - Testing Floor Comm");

        DatagramSocket socket = new DatagramSocket();

        byte[] receiveData = new byte[10];
        DatagramPacket recvPacket = new DatagramPacket(receiveData, receiveData.length);

        sct.start();

        socket.send(reqPacket);

        socket.receive(recvPacket);

        assertEquals(new String(receiveData).trim(), "NULL");

        socket.close();
    }

    /**
     * Tests the Scheduler's integration with the elevator subsystem.
     *
     * Tests:
     * Pretend to be an Elevator (socket) and send random status to the Scheduler.
     * Ensure that the response makes sense.
     *
     * THIS TEST MAY CONFLICT WITH OTHERS DUE TO SOCKET INITIALIZATION.
     *
     * @throws IOException Socket-related errors
     */
    @Test
    public void testSchedulerIntegrationElevator() throws IOException {
        Scheduler sc = new Scheduler(false, null);
        Thread sct = new Thread(sc, "Scheduler - Testing Floor Comm");

        DatagramPacket sendPacket1 = eso1.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
        DatagramPacket sendPacket2 = eso2.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
        DatagramPacket sendPacket3 = eso3.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);
        DatagramPacket sendPacket4 = eso4.toPacket(SystemConfigConstants.SCHEDULER_SUBSYSTEM_BACKWARD_PORT);

        DatagramPacket recvPacket1 = new DatagramPacket(new byte[100], 100);
        DatagramPacket recvPacket2 = new DatagramPacket(new byte[100], 100);
        DatagramPacket recvPacket3 = new DatagramPacket(new byte[100], 100);
        DatagramPacket recvPacket4 = new DatagramPacket(new byte[100], 100);

        ElevatorStatusObj recv1, recv2, recv3, recv4;

        DatagramSocket socket = new DatagramSocket();

        sct.start();

        // send the first status object
        socket.send(sendPacket1);
        socket.receive(recvPacket1);
        recv1 = ElevatorStatusObj.fromPacket(recvPacket1);

        // send the second status object
        socket.send(sendPacket2);
        socket.receive(recvPacket2);
        recv2 = ElevatorStatusObj.fromPacket(recvPacket2);

        // send the third status object
        socket.send(sendPacket3);
        socket.receive(recvPacket3);
        recv3 = ElevatorStatusObj.fromPacket(recvPacket3);

        // send the fourth status object
        socket.send(sendPacket4);
        socket.receive(recvPacket4);
        recv4 = ElevatorStatusObj.fromPacket(recvPacket4);

        socket.close();

        // check the first received (should be the same)
        assertEquals(eso1.getElevatorID(), recv1.getElevatorID());
        assertEquals(eso1.getState(), recv1.getState());
        assertEquals(eso1.isDoorErrorFlag(), recv1.isDoorErrorFlag());
        assertEquals(eso1.isMotorErrorFlag(), recv1.isMotorErrorFlag());
        assertEquals(eso1.getFloor(), recv1.getFloor());

        // check the second received
        assertEquals(eso2.getElevatorID(), recv2.getElevatorID());
        assertEquals(eso2.getState(), recv2.getState());
        assertEquals(eso2.isDoorErrorFlag(), recv2.isDoorErrorFlag());
        assertEquals(eso2.isMotorErrorFlag(), recv2.isMotorErrorFlag());
        assertEquals(eso2.getFloor(), recv2.getFloor());

        // check the third received
        assertEquals(eso3.getElevatorID(), recv3.getElevatorID());
        assertEquals(eso3.getState(), recv3.getState());
        assertEquals(eso3.isDoorErrorFlag(), recv3.isDoorErrorFlag());
        assertEquals(eso3.isMotorErrorFlag(), recv3.isMotorErrorFlag());
        assertEquals(eso3.getFloor(), recv3.getFloor());

        // check the fourth received
        assertEquals(eso4.getElevatorID(), recv4.getElevatorID());
        assertEquals(eso4.getState(), recv4.getState());
        assertEquals(eso4.isDoorErrorFlag(), recv4.isDoorErrorFlag());
        assertEquals(eso4.isMotorErrorFlag(), recv4.isMotorErrorFlag());
        assertEquals(eso4.getFloor(), recv4.getFloor());
    }
}
