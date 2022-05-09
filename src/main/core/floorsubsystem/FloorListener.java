package main.core.floorsubsystem;

/**
 * This interface provides a generalization for FloorSubsystem views.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public interface FloorListener {

    /**
     * Update the light on a floor.
     *
     * @param floor The floor to update
     * @param up Whether it is the up light or down light
     * @param on Whether the light is on or off
     */
    void update(int floor, boolean up, boolean on);

    /**
     * Add a new log.
     *
     * @param logText The log to add
     */
    void updateLog(String logText);
}
