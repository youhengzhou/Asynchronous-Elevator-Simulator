package main.core.elevatorsubsystem;

import main.exceptions.ElevatorStatusObjConversionException;
import main.exceptions.ElevatorStatusObjInitializationException;
import main.util.PacketBuilder;
import main.util.constants.BuildingConfigConstants;
import main.util.constants.ElevatorConfigConstants;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This class provides a data structure for usage in communicating between Scheduler and Elevator.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorStatusObj {

    /**
     * The id of the sender
     */
    private int[] senderID;

    /**
     * The id of the elevator that originally sent
     */
    private final int elevatorID;

    /**
     * The current floor of the elevator
     */
    private int floor;

    /**
     * The destination queue of the elevator
     */
    private Queue<Integer> destinations;

    /**
     * The set of panel lights that are on at a given time
     */
    private Set<Integer> panelLights;

    /**
     * The new state of the elevator
     */
    private ElevatorState state;

    /**
     * Flag if a motor error will occur in the future
     */
    private boolean motorErrorFlag;

    /**
     * Flag if a door error will occur in the future
     */
    private boolean doorErrorFlag;

    /**
     * Builder for packets
     */
    private final PacketBuilder packetBuilder;

    /**
     * Default constructor for instances of ElevatorStatusObj.
     * Initializes a new message with the associated elevator values.
     *
     * @param senderID The ID of the sender (when sending via packets)
     * @param elevatorID The ID of the elevator (elevator number)
     */
    public ElevatorStatusObj(int[] senderID, int elevatorID) {
        this(senderID, elevatorID, ElevatorConfigConstants.STARTING_FLOORS[elevatorID], new LinkedList<>(), new HashSet<>(), ElevatorState.DOORS_OPEN, false, false);
    }

    /**
     * Another constructor for instances of ElevatorStatusObj.
     * Initializes a new message with the associated elevator values.
     *
     * @param senderID The ID of the entity using this object (anything)
     * @param elevatorID The ID of the elevator that this object is associated with
     * @param floor The current floor of the elevator
     * @param destinations The destination queue for the elevator
     * @param panelLights The unordered collection of panel lights that are on
     * @param elevatorState The state of the elevator
     */
    public ElevatorStatusObj(int[] senderID, int elevatorID, int floor, Queue<Integer> destinations, Set<Integer> panelLights, ElevatorState elevatorState) {
        this(senderID, elevatorID, floor, destinations, panelLights, elevatorState, false, false);
    }

    /**
     * Another constructor for instances of ElevatorStatusObj.
     * Initializes a new message with the associated elevator values.
     *
     * @param senderID The ID of the entity using this object (anything)
     * @param elevatorID The ID of the elevator that this object is associated with
     * @param floor The current floor of the elevator
     * @param destinations The destination queue for the elevator
     * @param panelLights The unordered collection of panel lights that are on
     * @param elevatorState The state of the elevator
     * @param doorErrorFlag The status of the door error flag
     * @param motorErrorFlag The status of the motor error flag
     */
    public ElevatorStatusObj(int[] senderID, int elevatorID, int floor, Queue<Integer> destinations, Set<Integer> panelLights, ElevatorState elevatorState, boolean doorErrorFlag, boolean motorErrorFlag) {
        packetBuilder = new PacketBuilder();
        if (senderID.length != 2) throw new ElevatorStatusObjInitializationException("A ElevatorStatusObj must be initialized with a senderID of length 2.");
        int minID = 0;
        int maxID = BuildingConfigConstants.NUMBER_OF_ELEVATORS;
        if (elevatorID < minID || elevatorID > maxID-1) throw new ElevatorStatusObjInitializationException("A ElevatorStatusObj must be initialized with a valid elevatorID (out of bounds).");
        int minFloor = 1;
        int maxFloor = BuildingConfigConstants.NUMBER_OF_FLOORS;
        if (floor < minFloor || floor > maxFloor) throw new ElevatorStatusObjInitializationException("A ElevatorStatusObj must be initialized with a valid starting floor (out of bounds).");

        this.senderID = senderID;
        this.elevatorID = elevatorID;
        this.floor = floor;
        this.destinations = destinations;
        this.panelLights = panelLights;
        this.state = elevatorState;
        this.doorErrorFlag = doorErrorFlag;
        this.motorErrorFlag = motorErrorFlag;
    }

    /**
     * Sets the destinations of this message.
     *
     * @param dests The new list of destinations
     */
    public void setDestinations(Queue<Integer> dests) {
        this.destinations = dests;
    }

    /**
     * Retrieves the Elevator's queue of destinations from this message.
     *
     * @return The destination queue
     */
    public Queue<Integer> getDestinations() {
        return destinations;
    }

    /**
     * Sets the destinations of this message.
     *
     * @param panelLights The new list of destinations
     */
    public void setPanelLights(Set<Integer> panelLights) {
        this.panelLights = panelLights;
    }

    /**
     * Retrieves the Elevator's queue of destinations from this message.
     *
     * @return The destination queue
     */
    public Set<Integer> getPanelLights() {
        return panelLights;
    }

    /**
     * Sets the message's elevator state.
     *
     * @param es The new state of the elevator
     */
    public void setState(ElevatorState es) {
        this.state = es;
    }

    /**
     * Retrieves the Elevator's new state from this message.
     *
     * @return The new state
     */
    public ElevatorState getState() {
        return state;
    }

    /**
     * Get the error state boolean variable
     *
     * @return true if elevator is in error state, return true
     */
    public boolean isInErrorState(){
        if(this.getState()==ElevatorState.DOOR_ERROR || this.getState()==ElevatorState.MOTOR_ERROR){
            return true;
        }
        return false;
    }

    /**
     * Retrieves the ID of the elevator associated with this message.
     *
     * @return The elevator id
     */
    public int getElevatorID() {
        return elevatorID;
    }

    /**
     * Retrieves the Elevator's current floor from this message.
     *
     * @return The current floor
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Sets the current floor of the elevator within this message.
     *
     * @param floor The current floor to set
     */
    public void setFloor(int floor) {
        this.floor = floor;
    }

    /**
     * Retrieves the senderID of this message.
     *
     * @return The senderID
     */
    public int[] getSenderID() {
        return this.senderID;
    }
    
    /**
     * Retrieves the door error flag of this message.
     *
     * @return The door error flag
     */
    public boolean isInDoorErrorState() {
        return this.state == ElevatorState.DOOR_ERROR;
    }

    /**
     * Sets the senderID of this message.
     *
     * @param senderID The senderID
     */
    public void setSenderID(int[] senderID) {
        this.senderID = senderID;
    }

    /**
     * Clears destinations.
     */
    public void clearDestinations() {
        destinations.clear();
    }

    /**
     * Clears destinations for light buttons.
     */
    public void clearPanelLights() {
        panelLights.clear();
    }

    /**
     * Adds destinations to the list.
     *
     * @param dests The destinations to add
     */
    public void addDestinations(Collection<Integer> dests) {
        destinations.addAll(dests);
    }

    /**
     * Adds destinations for elevator button lights to the list.
     *
     * @param dests The destinations to add
     */
    public void addPanelLights(Collection<Integer> dests) {
        panelLights.addAll(dests);
    }

    /**
     * Adds a single destination to the list.
     *
     * @param dest The destination to add
     */
    public void addDestination(int dest) {
        destinations.add(dest);
    }

    /**
     * Adds a single destination button light to the list.
     *
     * @param dest The destination to add
     */
    public void addPanelLight(int dest) {
        panelLights.add(dest);
    }

    /**
     * Increments the current floor by 1
     */
    public void incrementFloor() {
        floor += 1;
    }

    /**
     * Decrements the current floor by 1.
     */
    public void decrementFloor() {
        floor -= 1;
    }

    /**
     * Determines if the elevator is currently in a moving state.
     *
     * @return Whether or not the elevator is moving
     */
    public boolean isMoving() {
        return (state == ElevatorState.MOVING_UP || state == ElevatorState.MOVING_DOWN);
    }

    /**
     * Provides a textual representation of this message between Scheduler and Elevator.
     *
     * @return The textual representation of the message
     */
    @Override
    public String toString() {
        return String.format(
                "*Elevator-Scheduler Message*\ncurrent floor: %s\ndestination queue: %s\npanel lights: %s\nstate: %s\nderr. flag: %s\nmerr. flag: %s\n",
                floor, destinations, panelLights, state, doorErrorFlag, motorErrorFlag
        );
    }

    /**
     * Translates a ElevatorSchedulerMessage object into a packet ready to be sent.
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
            packetBuilder.addFlag(doorErrorFlag);
            packetBuilder.addFlag(motorErrorFlag);
            packetBuilder.addInts(new int[]{elevatorID, floor, state.name().length()});
            packetBuilder.addString(state.name());
            packetBuilder.addList(destinations, true);
            packetBuilder.addList(panelLights, true);
            packetBuilder.makeSendPacket(addr, port);
            DatagramPacket pkt = packetBuilder.getPacket();
            packetBuilder.clear();
            return pkt;
        } catch (Exception e) {
            throw new ElevatorStatusObjConversionException(true, e.toString());
        }
    }

    /**
     * Translates a ElevatorSchedulerMessage object into a packet ready to be sent (local host).
     *
     * @param port The port of the destination
     * @return The packet form of the object
     * @throws UnknownHostException When the local host can not be resolved
     */
    public DatagramPacket toPacket(int port) throws UnknownHostException {
        return toPacket(InetAddress.getLocalHost(), port);
    }

    /**
     * Translates a packet into and ElevatorSchedulerMessage object.
     *
     * @param packet The packet to translate
     * @return The ElevatorSchedulerMessage object translation
     */
    public static ElevatorStatusObj fromPacket(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();
            int[] identifier = new int[]{(int) data[0], (int) data[1]};
            int doorErrorFlag = data[3];
            int motorErrorFlag = data[5];
            int elevatorID = data[7];
            int currentFloor = data[9];
            int stateLen = data[11];
            String state = new String(data, 13, stateLen);
            int destSize = data[stateLen + 14];
            LinkedList<Integer> destinations = new LinkedList<>();
            Set<Integer> destinationsLights = new HashSet<>();
            int destFin = stateLen+destSize+16;
            for (int i = stateLen + 16; i < destFin; i++) { // TODO: changed from 12 to 14
                destinations.add((int) data[i]);
            }
            int lightsLen = data[destFin+1];
            for (int i = destFin + 3; i < destFin+lightsLen+3; i ++)
                destinationsLights.add((int) data[i]);
            return new ElevatorStatusObj(identifier, elevatorID, currentFloor, destinations, destinationsLights, ElevatorState.valueOf(state), (doorErrorFlag == 1), (motorErrorFlag == 1));
        } catch (Exception e) {
            throw new ElevatorStatusObjConversionException(false, e.toString());
        }
    }

    /**
     * Retrieve the current status of the motor error flag.
     *
     * @return The status of the motor error flag
     */
    public boolean isMotorErrorFlag() {
        return motorErrorFlag;
    }

    /**
     * Sets the current status of the motor error flag.
     *
     * @param motorErrorFlag The motor error flag status
     */
    public void setMotorErrorFlag(boolean motorErrorFlag) {
        this.motorErrorFlag = motorErrorFlag;
    }

    /**
     * Retrieve the current status of the door error flag.
     *
     * @return The status of the door error flag
     */
    public boolean isDoorErrorFlag() {
        return doorErrorFlag;
    }

    /**
     * Sets the current status of the door error flag.
     *
     * @param doorErrorFlag The status of the door error flag
     */
    public void setDoorErrorFlag(boolean doorErrorFlag) {
        this.doorErrorFlag = doorErrorFlag;
    }
}
