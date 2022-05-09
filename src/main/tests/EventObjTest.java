package main.tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.Timestamp;
import org.junit.Test;
import static org.junit.Assert.*;
import main.core.floorsubsystem.EventObj;

/**
 * Testing class for the EventObj.
 * Tests the functions asynchronously.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class EventObjTest {

    private static final int[] SENDERID = new int[] {0, 1};
    private static final int[] RECVID = new int[] {0, 2};

    @Test
    public void testToFromPacket() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        EventObj eo = new EventObj(SENDERID, ts.toString(), 0, "Up", 6, 0, 1);
        try {
            DatagramPacket eoPack = eo.toPacket(InetAddress.getLocalHost(),25);
            EventObj recv = EventObj.fromPacket(eoPack);
            assertEquals(eo, recv);
            recv.setSenderID(RECVID);
            eoPack = recv.toPacket(InetAddress.getLocalHost(),25);
            eo = EventObj.fromPacket(eoPack);
            assertEquals(recv, eo);
        } catch (IOException e) {
            System.out.println("EventObj could not be converted into local host packet.");
            System.exit(1);
        }
    }
}
