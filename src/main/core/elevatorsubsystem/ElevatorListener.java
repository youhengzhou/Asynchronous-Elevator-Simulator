package main.core.elevatorsubsystem;

/**
 * A general interface for those views who wish to listen to elevators.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public interface ElevatorListener {

    /**
     * A general update function for the views.
     *
     * @param eso The status object to update with
     * @param logText The log text to update
     */
    void update(ElevatorStatusObj eso, String logText);
}
