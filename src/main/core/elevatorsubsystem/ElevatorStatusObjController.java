package main.core.elevatorsubsystem;

/**
 * This class implements controller for the passing of information between a elevator model and view.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorStatusObjController {

    /**
     * The linked view (only one).
     */
    private ElevatorListener view;

    /**
     * Default constructor for instances of SchedulerController.
     */
    public ElevatorStatusObjController(){
        this.view = null;
    }

    /**
     * Links the view to this controller.
     *
     * @param view The given view
     */
    public void setListener(ElevatorListener view) {
        this.view = view;
    }

    /**
     * Update when receiving an event related to the status of an elevator.
     *
     * @param eso The status object of the elevator (contains ID)
     * @param log Any log text for the elevator
     */
    public void updateView(ElevatorStatusObj eso, String log){
        if (view != null)
            view.update(eso, log);
    }

}
