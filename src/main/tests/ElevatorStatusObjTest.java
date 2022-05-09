package main.tests;

import static org.junit.Assert.*;

import main.core.elevatorsubsystem.ElevatorStatusObj;
import main.core.elevatorsubsystem.ElevatorState;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Tests the functionality behind the ElevatorSchedulerMessage.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorStatusObjTest {
    @Test
    public void test() {
        LinkedList<Integer> dests = new LinkedList<>();
        dests.add(1);
        dests.add(2);
        dests.add(3);
        Set<Integer> destsLights = new HashSet<>();
        destsLights.add(2);
        ElevatorStatusObj es = new ElevatorStatusObj(new int[]{0, 3}, 1, 1, dests, destsLights, ElevatorState.IDLE, true, false);
        try {
            DatagramPacket pkt = es.toPacket(InetAddress.getLocalHost(), 23);
            ElevatorStatusObj recv = ElevatorStatusObj.fromPacket(pkt);
            assertEquals(recv.getFloor(), es.getFloor());
            assertEquals(recv.getState(), es.getState());
            int[] recvSenderID = recv.getSenderID();
            int[] esSenderID = es.getSenderID();
            for (int i = 0; i < esSenderID.length; i ++) {
                assertEquals(recvSenderID[i], esSenderID[i]);
            }
            for (int i = 0; i < es.getDestinations().size(); i ++) {
                assertEquals(recv.getDestinations().poll(), es.getDestinations().poll());
            }
            System.out.println(recv);
            for (int i = 0; i < es.getPanelLights().size(); i ++) {
                assertEquals(recv.getPanelLights().remove(0), es.getPanelLights().remove(0));
            }
            assertEquals(recv.isMotorErrorFlag(), es.isMotorErrorFlag());
            assertEquals(recv.isDoorErrorFlag(), es.isDoorErrorFlag());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        LinkedList<Integer> dests = new LinkedList<>();
        dests.add(1);
        dests.add(2);
        dests.add(3);
        dests.add(4);
        dests.add(5);
        Set<Integer> destsLights = new HashSet<>();
        ElevatorStatusObj es = new ElevatorStatusObj(new int[]{0, 3}, 1, 1, dests, destsLights, ElevatorState.IDLE);
        try {
            DatagramPacket pkt = es.toPacket(InetAddress.getLocalHost(), 23);
            ElevatorStatusObj recv = ElevatorStatusObj.fromPacket(pkt);
            assertEquals(recv.getFloor(), es.getFloor());
            assertEquals(recv.getState(), es.getState());
            int[] recvSenderID = recv.getSenderID();
            int[] esSenderID = es.getSenderID();
            for (int i = 0; i < esSenderID.length; i ++) {
                assertEquals(recvSenderID[i], esSenderID[i]);
            }
            for (int i = 0; i < es.getDestinations().size(); i ++) {
                assertEquals(recv.getDestinations().poll(), es.getDestinations().poll());
            }
            assertEquals(recv.getPanelLights().isEmpty(), es.getPanelLights().isEmpty());
            assertEquals(recv.isMotorErrorFlag(), es.isMotorErrorFlag());
            assertEquals(recv.isDoorErrorFlag(), es.isDoorErrorFlag());
            System.out.println(recv);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        LinkedList<Integer> dests = new LinkedList<>();
        dests.add(1);
        dests.add(2);
        dests.add(3);
        dests.add(4);
        dests.add(5);
        Set<Integer> destsLights = new HashSet<>();
        ElevatorStatusObj es = new ElevatorStatusObj(new int[]{0, 3}, 1, 1, dests, destsLights, ElevatorState.IDLE, true, true);
        try {
            DatagramPacket pkt = es.toPacket(InetAddress.getLocalHost(), 23);
            ElevatorStatusObj recv = ElevatorStatusObj.fromPacket(pkt);
            System.out.println(es);
            System.out.println(recv);
            assertEquals(recv.getFloor(), es.getFloor());
            assertEquals(recv.getState(), es.getState());
            System.out.println(Arrays.toString(pkt.getData()));
            int[] recvSenderID = recv.getSenderID();
            int[] esSenderID = es.getSenderID();
            for (int i = 0; i < esSenderID.length; i ++) {
                assertEquals(recvSenderID[i], esSenderID[i]);
            }
            for (int i = 0; i < es.getDestinations().size(); i ++) {
                assertEquals(recv.getDestinations().poll(), es.getDestinations().poll());
            }

            assertEquals(recv.getPanelLights().isEmpty(), es.getPanelLights().isEmpty());
            assertEquals(recv.isDoorErrorFlag(), es.isDoorErrorFlag());
            assertEquals(recv.isMotorErrorFlag(), es.isMotorErrorFlag());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
