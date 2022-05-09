package main.core.floorsubsystem;

import main.exceptions.FloorInitializationException;
import main.exceptions.FloorLightNotAccessibleException;

/**
 * This class provides an implementation for the Floors in the FloorSubsystem.
 * Needs remodelling, does not currently work.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class Floor {

    /**
     * The ID of the floor.
     */
    private int floorID;

    /**
     * Represents the status of the up light
     */
    private int upLight;

    /**
     * Represents the status of the down light
     */
    private int downLight;

    /**
     * An integer to represent the value of a light that is not accessible.
     */
    public static final int LIGHT_NOT_ACCESSIBLE = -1;

    /**
     * Default constructor for instances of Floor.
     * Initializes a new Floor with the given ID.
     *
     * @param floorID The ID of this floor
     * @param hasDownLight Whether this floor has a down light or not
     * @param hasUpLight Whether this floor has an up light or not
     */
    public Floor(int floorID, boolean hasDownLight, boolean hasUpLight) {
        if (!hasDownLight & !hasUpLight) {
            throw new FloorInitializationException("Floor can not be initialized with no up light and no down light.");
        } else if (!hasDownLight) {
            this.downLight = LIGHT_NOT_ACCESSIBLE;
            this.upLight = 0;
        } else if (!hasUpLight) {
            this.downLight = 0;
            this.upLight = LIGHT_NOT_ACCESSIBLE;
        }
        this.floorID = floorID;
    }

    /**
     * Retrieves the ID of the floor.
     *
     * @return The floor ID
     */
    public int getFloorID() {
        return floorID;
    }

    /**
     * Sets the up light on.
     */
    public void turnOnUpLight() {
        if (this.upLight == LIGHT_NOT_ACCESSIBLE)
            throw new FloorLightNotAccessibleException(true);
        else
            this.upLight = 1;
    }

    /**
     * Sets the up light off.
     */
    public void turnOffUpLight() {
        if (this.upLight == LIGHT_NOT_ACCESSIBLE)
            throw new FloorLightNotAccessibleException(true);
        else
            this.upLight = 0;
    }

    /**
     * Determines if the up light is on.
     *
     * @return up light status
     */
    public boolean isUpLight() {
        if (this.upLight == LIGHT_NOT_ACCESSIBLE) {
            System.out.println("Not accessible");
            throw new FloorLightNotAccessibleException(true);
        }
        else
            return (upLight == 1);
    }

    /**
     * Sets the down light to on.
     */
    public void turnOnDownLight() {
        if (this.downLight == LIGHT_NOT_ACCESSIBLE)
            throw new FloorLightNotAccessibleException(false);
        else
            this.downLight = 1;
    }

    /**
     * Sets the down light to off.
     */
    public void turnOffDownLight() {
        if (this.downLight == LIGHT_NOT_ACCESSIBLE)
            throw new FloorLightNotAccessibleException(false);
        else
            this.downLight = 0;
    }

    /**
     * Determines if the down light is on.
     *
     * @return down light status
     */
    public boolean isDownLight() {
        if (this.downLight == LIGHT_NOT_ACCESSIBLE)
            throw new FloorLightNotAccessibleException(false);
        else
            return (downLight == 1);
    }
}
