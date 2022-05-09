package main.core.scheduler;

/**
 * This class implements controller for the passing of information between a scheduler model and the scheduler view.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class SchedulerController {

    /**
     * The linked view (only one).
     */
    private SchedulerListener view;

    /**
     * Default constructor for instances of SchedulerController.
     */
    public SchedulerController(){
        this.view = null;
    }

    /**
     * Links the view to this controller.
     *
     * @param view The given view
     */
    public void setListener(SchedulerListener view) {
        this.view = view;
    }

    /**
     * Updates the view, if there is any.
     *
     * @param time The timestamp
     * @param thNum The thread id
     * @param logText The log information
     */
    public void updateView(String time, long thNum, String logText){
        if (view != null)
            view.update(time, thNum, logText);
    }
}
