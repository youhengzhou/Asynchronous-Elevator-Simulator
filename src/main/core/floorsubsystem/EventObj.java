package main.core.floorsubsystem;

import main.exceptions.ElevatorStatusObjConversionException;
import main.exceptions.EventObjConversionException;
import main.util.PacketBuilder;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class provides an implementation of an object meant
 * for storing the details of Elevator system-related events.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class EventObj {

	/**
	 * The id os the sender of this message
	 */
	private int[] senderID;

	/**
	 * The time that this event occurred at
	 */
	private String time;
	
	/**
	 * The floor that this event was called from
	 */
	private int floor;
	
	/**
	 * The direction that the car will be going
	 */
	private boolean floorButton;
	
	/**
	 * The floor that this event is going to
	 */
	private int carButton;
	
	/**
	 * Whether there is a door error occurring with this event or not
	 */
	private boolean doorError;
	
	/**
	 * Whether there is a motor error occurring with this event or not
	 */
	private boolean motorError;

	/**
	 * Builder for packets
	 */
	private static PacketBuilder packetBuilder;
	
	/**
	 * Default constructor for instances of EventObj.
	 * Initializes a new Event object with the given event details.
	 * 
	 * @param time The time of the event
	 * @param floor The floor that the event occurred on
	 * @param floorButton The direction of the event
	 * @param carButton The destination floor of the event
	 * @param doorError Whether this event will include a door error or not
	 * @param motorError Whether this event will include a motor error or not
	 */
	public EventObj(int[] senderID, String time, int floor, String floorButton, int carButton, int doorError, int motorError) {
		packetBuilder = new PacketBuilder();
		this.senderID = senderID;
		this.time = time;
		this.floor = floor;
		this.floorButton = floorButton.equals("Up");
		this.carButton = carButton;
		this.doorError = doorError == 1;
		this.motorError = motorError == 1;
	}
	
	/**
	 * Retrieves the time of the event.
	 * 
	 * @return The time of the event
	 */
	public String getTime() {
		return this.time;
	}
	
	/**
	 * Retrieves the floor of the event.
	 * 
	 * @return The floor of the event
	 */
	public int getFloor() {
		return this.floor;
	}
	
	/**
	 * Retrieves the direction of the event.
	 * 
	 * @return The direction of the event
	 */
	public boolean getFloorButton() {
		return this.floorButton;
	}
	
	/**
	 * Retrieves the destination floor of the event.
	 * 
	 * @return The destination floor of the event
	 */
	public int getCarButton() {
		return this.carButton;
	}
	
	/**
	 * Retrieves the door error value.
	 * 
	 * @return The door error value
	 */
	public boolean getDoorError() {
		return this.doorError;
	}
	
	/**
	 * Retrieves the motor error value.
	 * 
	 * @return The motor error value
	 */
	public boolean getMotorError() {
		return this.motorError;
	}

	/**
	 * Retrieves the senderID of this EventObj.
	 *
	 * @return The senderID
	 */
	public int[] getSenderID() {
		return this.senderID;
	}

	/**
	 * Sets the senderID of this EventObj.
	 *
	 * @param senderID The senderID
	 */
	public void setSenderID(int[] senderID) {
		this.senderID = senderID;
	}

	/**
	 * Translates an EventObj object into a packet ready to be sent.
	 *
	 * @param addr The destination address of the packet
	 * @param port The port of the destination
	 * @return The packet form of the object
	 */
	public DatagramPacket toPacket(InetAddress addr, int port) {
		// Sender
		try {
			packetBuilder.clear();
			packetBuilder.addIdentifier(senderID);
			packetBuilder.addInt(this.time.length());
			packetBuilder.addString(this.time);
			packetBuilder.addInt(floor);
			packetBuilder.addFlag(floorButton);
			packetBuilder.addInt(carButton);
			packetBuilder.addFlag(doorError);
			packetBuilder.addFlag(motorError);
			packetBuilder.makeSendPacket(addr, port);
			DatagramPacket pkt = packetBuilder.getPacket();
			packetBuilder.clear();
			return pkt;
		} catch (Exception e) {
			throw new EventObjConversionException(true, e.toString());
		}
	}

	/**
	 * Translates an EventObj object into a packet ready to be sent (local host).
	 *
	 * @param port The port of the destination
	 * @return The packet form of the object
	 * @throws UnknownHostException When the local host can not be resolved
	 */
	public DatagramPacket toPacket(int port) throws UnknownHostException {
		return toPacket(InetAddress.getLocalHost(), port);
	}

	/**
	 * Translates a packet into and EventObj object.
	 *
	 * @param packet The packet to translate
	 * @return The EventObj object translation
	 */
	public static EventObj fromPacket(DatagramPacket packet) {
		byte[] data = packet.getData();
		try {
			int[] identifier = new int[]{(int) data[0], (int) data[1]};
			int timeLength = data[3];
			String time = new String(data, 5, timeLength);
			int posAft = timeLength + 6;
			int floor = data[posAft];
			int dir = data[posAft + 2];
			int carB = data[posAft + 4];
			int dErr = data[posAft + 6];
			int mErr = data[posAft + 8];
			return new EventObj(identifier, time, floor, (dir == 1) ? "Up" : "Down", carB, dErr, mErr);
		} catch (Exception e) {
			throw new EventObjConversionException(false, e.toString());
		}
	}

	/**
	 * Removes the motor error flag from this event.
	 */
	public void removeMotorError(){
		this.motorError = false;
	}

	/**
	 * Removes the door error flag from this event.
	 */
	public void removeDoorError() {
		this.doorError = false;
	}

	/**
	 * Equals method for instances of EventObj.
	 * Asserts that the fields of one EventObj are equal to those of another.
	 *
	 * @param o The object to check equality of
	 * @return Whether the two objects are equal or not
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof EventObj) {
			EventObj eo = (EventObj) o;
			int[] eoID = eo.getSenderID();
			if (eoID.length == senderID.length) {
				for (int i = 0; i < senderID.length; i ++) {
					if (senderID[i] != eoID[i]) return false;
				}
			}
			if (!eo.getTime().equals(time)) return false;
			if (eo.getFloorButton() != floorButton) return false;
			if (eo.getFloor() != floor) return false;
			if (eo.getCarButton() != carButton) return false;
			if (eo.getDoorError() != doorError) return false;
			if (eo.getMotorError() != motorError) return false;
			return true;
		}
		return false;
	}
	
	/**
	 * Provides a textual representation of an event.
	 */
	@Override
	public String toString() {
		return String.format("*Event Object*\ntime: %s\nfloor: %s\nf-button: %s\nc-button: %s\nd-error: %s\nm-error: %s", this.time, this.floor, this.floorButton, this.carButton, this.doorError, this.motorError);
	}
}