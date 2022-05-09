package main.core.floorsubsystem;

/**
 * This class implements controller for the passing of information between a floor model and the floor view.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class FloorController {

    /**
     * The linked view (only one).
     */
    private FloorListener view;

    /**
     * Default constructor for instances of SchedulerController.
     */
    public FloorController() {
        this.view = null;
    }

    /**
     * Links the view to this controller.
     *
     * @param view The given view
     */
    public void setListener(FloorListener view) {
        this.view = view;
    }

    /**
     * Update when receiving an event related to the switching of a light.
     *
     * @param floor The floor for the event
     * @param up Whether it is the up light or down light
     * @param on Whether the light is to be switched on or off
     */
    public void updateViewLight(int floor, boolean up, boolean on) {
        if (view != null)
            view.update(floor, up, on);
    }

    /**
     * Update when receiving a log from the floor.
     *
     * @param logText The text to add
     */
    public void updateViewLog(String logText) {
        if (view != null)
            view.updateLog(logText);
    }


}
