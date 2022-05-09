package main.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A custom class designed for the incremental building of packets.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class PacketBuilder {

    /**
     * The bytes of the message to be sent.
     */
    private List<Byte> message;

    /**
     * The internal packet to build upon.
     */
    private DatagramPacket packet;

    /**
     * Whether this packet is being received or sent.
     */
    private boolean receiving;

    /**
     * Default constructor for instances of PacketBuilder.
     * Initializes a new PacketBuilder with an empty message.
     */
    public PacketBuilder() {
        message = new ArrayList<>();
    }

    /**
     * Adds a byte to the message.
     *
     * @param bt The byte to add
     */
    public void addByte(byte bt) {
        message.add(bt);
    }

    /**
     * Adds an integer identifier to the message.
     *
     * @param en The identifier to add
     */
    public void addIdentifier(int[] en) {
        for (int i : en) {
            message.add((byte) i);
        }
    }

    /**
     * Adds a string to the message.
     *
     * @param word The string to add
     */
    public void addString(String word) {

        message.add((byte) 0);
        for (byte b: word.getBytes(StandardCharsets.UTF_8)) {
            message.add(b);
        }
    }

    /**
     * Adds a single character to the message.
     *
     * @param c The character to add
     */
    public void addChar(char c) {
        message.add((byte) 0);
        message.add((byte) c);
    }

    /**
     * Adds a single int to the message.
     *
     * @param num The int to add
     */
    public void addInt(int num) {
        message.add((byte) 0);
        message.add((byte) num);
    }

    /**
     * Adds a single double to the message.
     *
     * @param num The double to add
     */
    public void addDouble(double num) {
        message.add((byte) 0);
        message.add((byte) num);
    }

    /**
     * Adds a single float to the message.
     *
     * @param num The float to add
     */
    public void addFloat(float num) {
        message.add((byte) 0);
        message.add((byte) num);
    }

    /**
     * Adds a single long to the message.
     *
     * @param num The long to add
     */
    public void addLong(long num) {
        message.add((byte) 0);
        message.add((byte) num);
    }

    /**
     * Adds a single short to the message.
     *
     * @param num The short to add
     */
    public void addShort(short num) {
        message.add((byte) 0);
        message.add((byte) num);
    }

    /**
     * Adds a boolean to the message.
     *
     * @param flag The boolean to add
     */
    public void addFlag(boolean flag) {
        message.add((byte) 0);
        message.add((byte) (flag ? 1 : 0));
    }

    /**
     * Adds multiple strings to the message.
     *
     * @param words The words to add
     */
    public void addStrings(String[] words) {
        for (String s: words) {
            addString(s);
        }
    }

    /**
     * Adds multiple strings to the message.
     *
     * @param i The iterable of strings to add
     */
    public void addStrings(Iterable<String> i) {
        for (String s: i) {
            addString(s);
        }
    }

    /**
     * Adds multiple ints to the message.
     *
     * @param nums The ints to add
     */
    public void addInts(int[] nums) {
        for (int i: nums) {
            addInt(i);
        }
    }

    /**
     * Adds multiple ints to the message.
     *
     * @param i The iterable of ints to add
     */
    public void addInts(Iterable<Integer> i) {
        for (Integer n: i) {
            addInt(n);
        }
    }

    /**
     * Adds a list of integers to the message (not separated).
     *
     * @param list The list of integers to add to the message in a sequence
     * @param addSize Whether to add the size of the list to the message or not
     */
    public void addList(Collection<Integer> list, boolean addSize) {
        if (addSize)
            addInt(list.size());
        if (!list.isEmpty()) {
            message.add((byte) 0);
            for (int i : list) {
                message.add((byte) i);
            }
        }
    }

    /**
     * Sets whether the packet is to be received or sent.
     *
     * @param receiving Whether the packet is being received or delivered
     */
    public void setReceiving(boolean receiving) {
        this.receiving = receiving;
    }

    /**
     * Turns the internal list into an array of bytes for the packet.
     *
     * @return The internal message in array form
     */
    private byte[] finalizeMessage() {
        byte[] m = new byte[message.size() + 1];
        for (int i = 0; i < m.length-1; i ++) {
            m[i] = message.get(i);
        }
        m[m.length-1] = (byte) 0;
        return m;
    }

    /**
     * Compiles the internal message into a packet, ready to send.
     *
     * @param addr The address the packet is being sent to
     * @param port The port of the destination address
     */
    public void makeSendPacket(InetAddress addr, int port) {
        byte[] data = finalizeMessage();
        packet = new DatagramPacket(data, data.length, addr, port);
    }

    /**
     * Compiles the internal message into a packet, ready to send to the local host.
     *
     * @param port The port of the local host to send to
     * @throws UnknownHostException When the local host can not be resolved
     */
    public void makeSendPacket(int port) throws UnknownHostException {
        byte[] data = finalizeMessage();
        packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), port);
    }

    /**
     * Retrieves the packet in string form.
     *
     * @param pckt The packet to retrieve string form of
     * @return A string of the packet
     */
    public static String packetToString(DatagramPacket pckt, boolean recv) {
        if (recv)
            return String.format("received host %s at port %s\nString: %s\nData: %s", pckt.getAddress(), pckt.getPort(), new String(pckt.getData(), 0, pckt.getLength()), Arrays.toString(pckt.getData()));
        else
            return String.format("is sending host %s at port %s\nString: %s\nData: %s", pckt.getAddress(), pckt.getPort(), new String(pckt.getData(), 0, pckt.getLength()), Arrays.toString(pckt.getData()));
    }

    /**
     * Retrieves the internal packet.
     *
     * @return The internal packet
     */
    public DatagramPacket getPacket() {
        return packet;
    }

    /**
     * Clears the internal message and packet.
     */
    public void clear() {
        message.clear();
        packet = null;
    }

    /**
     * Retrieves a message demonstrating the passing of this packet.
     *
     * @param pckt The packet that is being received or sent
     * @param host The host that the packet is being sent to
     * @param receiving Whether the packet is being received or delivered
     * @return The message string as described above
     */
    public static String packetPrintString(DatagramPacket pckt, String host, boolean receiving) {
        if (receiving)
            return String.format("%s received a packet from host %s at port %s\nString: %s\nData: %s",host,pckt.getAddress(), pckt.getPort(), new String(pckt.getData()), Arrays.toString(pckt.getData()));
        else
            return String.format("%s is sending a packet to host %s at port %s\nString: %s\nData: %s",host,pckt.getAddress(), pckt.getPort(), new String(pckt.getData()), Arrays.toString(pckt.getData()));
    }
}
